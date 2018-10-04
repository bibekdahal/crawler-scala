import java.net.URL

import org.jsoup.Jsoup

import collection.JavaConverters._

class UrlsExtractor(url: String) {
  private val USER_AGENT = "Mozilla/5.0 (compatible; Crawler/1.0; +http://crawler.com)"

  def extract: List[String] = {
    try {
      val document = Jsoup
        .connect(url)
        .userAgent(USER_AGENT)
        .get()
      val elements = document.select("a[href]").asScala.toList
      val urls = for (element <- elements) yield element.attr("href")

      urls.map(transformUrl)
    } catch {
      case _: Exception => List()
    }
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