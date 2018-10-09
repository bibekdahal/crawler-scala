import akka.actor.Actor

/*
Worker is the Actor handling the job of an independent crawler.
It takes a URL from the master and process that URL to retrieve more URLS by scraping.
 */
object Worker {
  // First some messages that the worker can handle
  abstract class WorkerMessage
  case class WorkAvailable() extends WorkerMessage
  case class ProcessPage(page: Page) extends WorkerMessage

  // Next the actual actor class
  class ActorClass(downloader: Downloader) extends Actor {
    // The message handlers:
    override def receive: Receive = {

      // Master notified us that a URL was available.
      // So send a request if it's still available.
      case WorkAvailable() =>
        sender() ! Master.RequestPage()

      // On behalf of our request, master has sent us a URL for crawling.
      case ProcessPage(page: Page) =>
        // First, validate the URL by verifying we can access it and checking the robots.txt rules
        if (UrlValidator.validate(page.url)) {
          // Download file from the validated URL
          downloader.download(page.url)
          // Extract new list of URLs by scraping
            val urls = new UrlsExtractor(page.url).extract
            // The new URLs now have depth, current + 1
          val pages = urls.map(Page(_, page.depthLevel + 1))

          // Send the new URLs to the master
          sender() ! Master.OnNewPages(pages)
        }
    }
  }
}