import java.net.{MalformedURLException, URL}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

object UrlValidator {
  private val USER_AGENT = "Mozilla/5.0 (compatible; Crawler/1.0; +http://crawler.com)"

  private type SiteRules = List[(String, String)]
  private val siteRules = mutable.HashMap[String, SiteRules]()

  def validate(url: String): Boolean = {
    try {
      // TODO: Check if sourceUrl protocol is http or https
      val sourceUrl = new URL(url)
      checkRuleFor(sourceUrl)
    } catch {
      case _: Exception => false
    }
  }

  private def checkRuleFor(url: URL): Boolean = {
    try {
      val path = url.getPath
      val key = url.getProtocol + "://" + url.getHost + ":" + url.getPort
      val rules = siteRules.getOrElseUpdate(key, parseRules(url))

      rules
        .find(r => r._2.r.findFirstMatchIn(path).isDefined)
        .forall(rule => {
          val allow = rule._2 != ""
          if (rule._1 == "allow") allow else !allow
        })
    } catch {
      case _: Exception => true
    }
  }

  private def regexify(str: String) = {
    if (str.length == 0) {
      ".*"
    } else {
      val tmp = str.replaceAll("\\*", ".*")
      "(?i)^" + tmp
    }
  }

  private def parseRules(url: URL): SiteRules = {
    val robotsUrl = new URL(url.getProtocol, url.getHost, url.getPort, "/robots.txt")
    val connection = robotsUrl.openConnection()
    connection.setRequestProperty("User-Agent", USER_AGENT)

    val document = Source.fromInputStream(connection.getInputStream).getLines().mkString("\n")
    val lines = document.split("\\r?\\n").map(l => l.trim.toLowerCase)

    val userAgentLength = "User-agent:".length
    val disallowLength = "Disallow:".length
    val allowLength = "Allow:".length

    var collectRules = false
    var rulesStarted = false
    val rules = ListBuffer[(String, String)]()

    lines.foreach(line => {
      if (collectRules) {
        if (line.startsWith("disallow:")) {
          rules += Tuple2("disallow", regexify(line.substring(disallowLength).trim))
          rulesStarted = true
        } else if (line.startsWith("allow:")) {
          rules += Tuple2("allow", regexify(line.substring(allowLength).trim))
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
  }
}
