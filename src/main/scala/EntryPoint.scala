import akka.actor.{ActorSystem, Props}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source


case class Page(url: String, nestLevel: Int)
case class WrongUsageException() extends Exception

object EntryPoint extends App {
  val kwargMap = mutable.HashMap[String, String]()
  val argList = ListBuffer[String]()

  def processArgs(args: List[String] = args.toList): Unit = args match {
    case key::value::tail if key.startsWith("--") =>
      kwargMap += (key -> value)
      processArgs(tail)
    case arg::tail =>
      argList += arg
      processArgs(tail)
    case Nil =>
  }

  try {
    processArgs()
    val maxDepth = kwargMap.getOrElse("--max-depth", "2").toInt
    val outputDir = kwargMap.get("--output")
    if (argList.isEmpty) throw WrongUsageException()

    val filename = argList.head
    val seedUrls = Source.fromFile(filename).getLines.toList

    val system = ActorSystem("crawler")
    val master = system.actorOf(Props(classOf[Master.ActorClass], seedUrls, maxDepth, outputDir), name = "master")
    master ! Master.Start()
  } catch {
    case _: WrongUsageException => println("Usage: --max-depth <number> --output <output-folder> <input_file>")
  }
}
