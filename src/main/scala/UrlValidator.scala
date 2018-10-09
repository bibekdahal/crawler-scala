import java.net.{HttpURLConnection, MalformedURLException, URL}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.matching.Regex

// Represents a rule in robots.txt
case class SiteRule(directive: String, path: Regex)

/*
Validate a URL to see if we can download and crawl the web page.
Among the things it do is check robots.txt file for crawling rules.
 */
object UrlValidator {
  // Each robots.txt gives a list of site rules
  private type SiteRules = List[SiteRule]

  // We will cache the rules for each website so that it can be used when the site is crawled again.
  private val siteRules = mutable.HashMap[String, SiteRules]()

  // Actual validation method
  def validate(url: String): Boolean = {
    try {
      // Try creating the URL object. This should throw an error if the URL is invalid
      val sourceUrl = new URL(url)
      // See if the protocol is one of the supported types: http or https
      val protocol = sourceUrl.getProtocol
      val isValidProtocol =  protocol == "http" || protocol == "https"
      // Finally check if we can access the URL and that the site-rules allow us to crawl the web page.
      isValidProtocol && canAccess(sourceUrl) && checkRuleFor(sourceUrl)
    } catch {
      case _: Exception => false
    }
  }

  // To check if we can access the URL, make a head request and see if it returns 200 OK status
  private def canAccess(url: URL): Boolean = {
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("HEAD")
    connection.setRequestProperty("User-Agent", Common.USER_AGENT)
    connection.getResponseCode == 200
  }

  // Method to check if the site rules allow us to crawl the web page at this URL
  private def checkRuleFor(url: URL): Boolean = {
    try {
      // We will need the path to match with the rules
      val path = url.getPath
      // This key is used to cache the robots.txt rules in our `siteRules` hash-map
      // and is composed of the protocol, host and the port.
      val key = url.getProtocol + "://" + url.getHost + ":" + url.getPort

      // Finally either get the cached rules or
      // if it isn't cached till now, get new ones from the robots.txt file.
      val rules = siteRules.getOrElseUpdate(key, parseRules(url))

      // Check if a rule exists that disallows this path
      rules
        .find(rule => rule.path.findFirstMatchIn(path).isDefined)
        .forall(rule => {
          val tmp = rule.directive != ""
          if (rule.directive == "allow") tmp else !tmp
        })
    } catch {
      case _: Exception => true
    }
  }

  // We need to convert the paths in robots.txt to a proper regex so that we can use them
  // to match with our paths later.
  private def regexify(str: String) = {
    if (str.length == 0) {
      ".*".r
    } else {
      val tmp = str.replaceAll("\\*", ".*")
      ("(?i)^" + tmp).r
    }
  }

  // Get the robots.txt and parse the rules
  private def parseRules(url: URL): SiteRules = {
    try {
      // Connect to the robots.txt file
      val robotsUrl = new URL(url.getProtocol, url.getHost, url.getPort, "/robots.txt")
      val connection = robotsUrl.openConnection()
      connection.setRequestProperty("User-Agent", Common.USER_AGENT)

      val document = Source.fromInputStream(connection.getInputStream).getLines().mkString("\n")
      // Get each line in the file in lower-case
      val lines = document.split("\\r?\\n").map(l => l.trim.toLowerCase)

      val userAgentLength = "User-agent:".length
      val disallowLength = "Disallow:".length
      val allowLength = "Allow:".length

      var collectRules = false
      var rulesStarted = false
      val rules = ListBuffer[(String, String)]()

      // This is kind of a state machine, with state variables: (rules, collectRules and rulesStarted)
      // Go through each line, check the starting string and according to current state, change the state variables.
      lines.foreach(line => {
        if (collectRules) {
          if (line.startsWith("disallow:")) {
            rules += Tuple2("disallow", line.substring(disallowLength).trim)
            rulesStarted = true
          } else if (line.startsWith("allow:")) {
            rules += Tuple2("allow", line.substring(allowLength).trim)
            rulesStarted = true
          } else if (rulesStarted && line.startsWith("user-agent:")) {
            val userAgent = line.substring(userAgentLength).trim
            if (userAgent != "*") {
              collectRules = false
            }
          }
        } else if (line.startsWith("user-agent:")) {
          val userAgent = line.substring(userAgentLength).trim
          if (userAgent == "*") {
            collectRules = true
            rulesStarted = false
          }
        }
      })

      // Sort the rules by longest path and prefer allow to disallow
      rules.sortWith((r1, r2) => {
        if (r1._2.length == r2._2.length) {
          r1._1 == "allow"
        } else {
          r1._2.length > r2._2.length
        }
      }).toList
        // Convert the tuples into SiteRule
        .map(r => SiteRule(r._1, regexify(r._2)))
    } catch {
      case _: Exception => {
        // If we cannot parse the rules, it probably because there is no proper robots.txt file.
        // In such case, we just assume that there is a rule that disallows nothing.
        List(SiteRule("disallow", regexify("")))
      }
    }
  }
}
