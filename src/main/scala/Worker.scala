import akka.actor.Actor

object Worker {
  case class WorkAvailable()
  case class ProcessPage(page: Page)

  class ActorClass(outputDir: Option[String]) extends Actor {
    override def receive: Receive = {
      case WorkAvailable() =>
        sender() ! Master.RequestPage()

      case ProcessPage(page: Page) =>
        if (UrlValidator.validate(page.url)) {
          new Downloader(outputDir, page.url).download()
          val urls = new UrlsExtractor(page.url).extract
          val pages = urls.map(Page(_, page.nestLevel + 1))
          sender() ! Master.OnNewPages(pages)
        }
    }
  }
}