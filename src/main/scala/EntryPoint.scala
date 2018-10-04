import akka.actor.{ActorSystem, Props}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source


case class Page(url: String, nestLevel: Int = 0)
case class WrongUsageException() extends Exception

object EntryPoint extends App {
  val kwargMap = mutable.HashMap[String, String]()
  val argList = ListBuffer[String]()

  def processArgs(args: List[String] = args.toList): Unit = args match {
    case key::value::tail if key.startsWith("--") =>
      kwargMap += (key -> value)
      processArgs(tail)
    // FIXME: should probably be just arg::tail
    case arg::Nil => argList += arg
    case Nil => throw WrongUsageException()
  }

  try {
    processArgs()
    val maxDepth = kwargMap.getOrElse("--max-depth", "2").toInt
    val filename = argList.head
    val seedUrls = Source.fromFile(filename).getLines.toList

    val system = ActorSystem("crawler")
    val master = system.actorOf(Props(classOf[Master.ActorClass], seedUrls, maxDepth), name = "master")
    master ! Master.Start()
  } catch {
    case _: WrongUsageException => println("Usage: --max-depth <number> <input_file>")
  }
}
