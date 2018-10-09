import java.net.URL
import org.jsoup.Jsoup
import collection.JavaConverters._

class UrlsExtractor(url: String) {
  def extract: List[String] = {
    try {
      val document = Jsoup
        .connect(url)
        .userAgent(Common.USER_AGENT)
        .get()
      val elements = document.select("a[href]").asScala.toList
      val urls = for (element <- elements) yield element.attr("href")

      urls.map(transformUrl).map(cleanUrl)
    } catch {
      case _: Exception => List()
    }
  }

  private def cleanUrl(str: String): String = {
    str.stripSuffix("#")
  }

  private def transformUrl(str: String): String = {
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
}