import akka.actor.{Actor, Props}

import scala.collection.mutable

/*
Master is an actor which is responsible for creating and coordinating
all the worker crawlers. It also maintains the list of completed URLs and
a queue of pending URLs.
 */
object Master {
  // First some messages that the master can handle
  abstract class MasterMessage
  case class Start() extends MasterMessage
  case class RequestPage() extends MasterMessage
  case class OnNewPages(pages: List[Page]) extends MasterMessage

  // Next the actual Actor class
  class ActorClass(seedUrls: List[String],    // List of initial URLs to start crawling
                   maxDepth: Int,             // Maximum depth to stop crawling
                   downloader: Downloader,    // Downloader capable of downloading files from URLs
                   scaleFactor: Float = 2     // Scaling factor to scale the number of workers
                  ) extends Actor {

    // Get the number of logical processors
    private val numProcessors = Runtime.getRuntime.availableProcessors()
    // Multiply it by scale to get the number of workers
    private val numWorkers = (numProcessors * scaleFactor).round

    // Create the actual worker actors
    private val workers = Array.fill(numWorkers) { context.actorOf(Props(classOf[Worker.ActorClass], downloader)) }

    // List of completed URLs and the queue of pending URLs
    // For a pending URL, it is helpful to know the depth of the crawling we have reached
    // when the URL was first retrieved. So we store this info in the form: Page(url, depth).
    private val completedUrls = mutable.TreeSet[String]()
    private val pendingPages = mutable.Queue[Page]()

    // The message handlers:
    override def receive: Receive = {
      // On start,
      // * fill the seed URLs into the pendingPages with depth set to zero
      // * tell each worker that a work has been available
      case Start() =>
        pendingPages ++= seedUrls.map(url => Page(url, 0))
        workers.foreach(_ ! Worker.WorkAvailable())

      // When a worker requests for a new page from the pending queue,
      // check if the queue is not empty, and if not give that page to the worker.
      // Note that wee also need to mark this URL as done by storing it in the completed list.
      case RequestPage() =>
        if (pendingPages.nonEmpty) {
          val page = pendingPages.dequeue()
          completedUrls += page.url
          sender() ! Worker.ProcessPage(page)
        }

      // When a worker gives the master a list of new pages,
      // store them in the pending queue and notify all workers that URLs are now available for processing.
      // We need to however first filter the incoming URLs by checking the following conditions:
      // * Depth is not more than the max-depth value
      // * The URL is not already in the completed list or the pending queue
      case OnNewPages(pages: List[Page]) =>
        pendingPages ++= pages
          .filter(page => page.depthLevel <= maxDepth && !containsUrl(page.url))
        workers.foreach(_ ! Worker.WorkAvailable())
    }

    private def containsUrl(url: String) =
      completedUrls.contains(url) ||
      pendingPages.exists(_.url == url)
  }
}
