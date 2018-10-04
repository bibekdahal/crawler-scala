import akka.actor.{Actor, Props}

import scala.collection.mutable

object Master {
  case class Start()
  case class RequestPage()
  case class OnNewPages(pages: List[Page])

  class ActorClass(seedUrls: List[String], maxDepth: Int) extends Actor {
    private val numCores = Runtime.getRuntime.availableProcessors()
    private val numWorkers = numCores * 2
    private val workers = Array.fill(numWorkers) { context.actorOf(Props[Worker.ActorClass]) }

    private val completedUrls = mutable.TreeSet[String]()
    private val pendingPages = mutable.Queue[Page]()
    pendingPages ++= seedUrls.map(url => Page(url, 0))

    override def receive: Receive = {
      case Start() =>
        workers.foreach(_ ! Worker.WorkAvailable())

      case RequestPage() =>
        if (pendingPages.nonEmpty) {
          val page = pendingPages.dequeue()
          completedUrls += page.url
          sender() ! Worker.ProcessPage(page)
        }

      case OnNewPages(pages: List[Page]) =>
        // TODO Write two filters with single AND
        pendingPages ++= pages
          .filter(page => page.nestLevel <= maxDepth)
          .filter(page => !completedUrls.contains(page.url))
        workers.foreach(_ ! Worker.WorkAvailable())
    }
  }
}
