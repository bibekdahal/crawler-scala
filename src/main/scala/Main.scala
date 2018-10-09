import akka.actor.{ActorSystem, Props}
import scala.io.Source

object Main extends App {
  // A recursive function to process command line arguments and generate a tuple of
  // keyword arguments as map and positional arguments as list
  private def processArgs(args: List[String],
                          kwargMap: Map[String, String] = Map(),
                          argList: List[String] = List()
                         ): (Map[String, String], List[String]) = args match {
    case key::value::tail if key.startsWith("--") =>
      val newMap = kwargMap + (key -> value)
      processArgs(tail, newMap, argList)
    case arg::tail =>
      val newList = arg :: argList
      processArgs(tail, kwargMap, newList)
    case Nil => (kwargMap, argList.reverse)
  }

  try {
    // Get the arguments
    val (kwargMap, argList) = processArgs(args.toList)
    val maxDepth = kwargMap.getOrElse("--max-depth", "2").toInt
    val scaleFactor = kwargMap.getOrElse("--scale", "2").toFloat
    val outputDir = kwargMap.get("--output")
    if (argList.isEmpty) throw WrongUsageException()

    // Take the first positional argument as the input file
    // and read the seed urls
    val filename = argList.head
    val seedUrls = Source.fromFile(filename).getLines.toList

    // Create and start the "master" actor which will be responsible
    // for managing all the workers
    val system = ActorSystem("crawler")
    val downloader = new Downloader(outputDir)
    val master = system.actorOf(Props(classOf[Master.ActorClass], seedUrls, maxDepth, downloader, scaleFactor), name = "master")
    master ! Master.Start()
  } catch {
    case _: WrongUsageException =>
      println("Usage: --max-depth <number> --output <output-folder> --scale <workers_scale_factor> <input_file>")
  }
}
