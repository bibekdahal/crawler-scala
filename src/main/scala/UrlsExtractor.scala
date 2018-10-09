import java.net.URL
import org.jsoup.Jsoup
import collection.JavaConverters._

/*
Class to extract URLs from a HTML page.
The HTML page is pointed out by the url passed when constructing the object of this class.
 */
class UrlsExtractor(url: String) {
  // Some post processing functions:

  // Clean URLs
  // Currently it only removes '#' from the end but we can put more actions here
  private val cleanUrl = (str: String) => str.stripSuffix("#")

  // Transform relative URLs to absolute
  private val transformUrl = (str: String) => {
    if (str.contains("://")) {
      str
    } else if (str.startsWith("/")) {
      val sourceUrl = new URL(url)
      new URL(sourceUrl.getProtocol, sourceUrl.getHost, sourceUrl.getPort, str)
        .toString
    } else if (url.endsWith("/")) {
      url + str
    } else {
      url + "/" + str
    }
  }

  // Composing the post processing functions into one
  private val postProcess = cleanUrl compose transformUrl

  // Actual extraction method uses Jsoup to scrape the hyperlinks
  def extract: List[String] = {
    try {
      val document = Jsoup
        .connect(url)
        .userAgent(Common.USER_AGENT)
        .get()
      val elements = document.select("a[href]").asScala.toList
      val urls = for (element <- elements) yield element.attr("href")

      // Return post processed URLs
      urls.map(postProcess)
    } catch {
      case _: Exception => List()
    }
  }
}