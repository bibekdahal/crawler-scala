
case class Page(url: String, depthLevel: Int)

object Common {
  val USER_AGENT = "Mozilla/5.0 (compatible; Crawler/1.0; +http://crawler.com)"
}

case class WrongUsageException() extends Exception
