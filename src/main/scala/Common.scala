
case class Page(url: String, nestLevel: Int)
case class WrongUsageException() extends Exception

object Common {
  val USER_AGENT = "Mozilla/5.0 (compatible; Crawler/1.0; +http://crawler.com)"
}
