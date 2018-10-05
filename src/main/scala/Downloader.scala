import sys.process._
import java.io.File
import java.net.{HttpURLConnection, URL}

import scala.language.postfixOps

class Downloader(outputDir: Option[String], url: String) {
  def download(): Unit = {
    println(url)
    if (outputDir.isDefined) {
      val sanitizedPath = url
        .replaceAll(":\\/\\/", "_")
        .replaceAll("\\/", "_")
        .replaceAll("\\.", "_")

      val dir = new File(outputDir.get)
      if (!dir.exists()) {
        dir.mkdir()
      }

      val file = new File(dir, sanitizedPath)

      val urlObject = new URL(url)
      val connection = urlObject.openConnection().asInstanceOf[HttpURLConnection]
      connection.setConnectTimeout(5000)
      connection.setReadTimeout(5000)
      connection.connect()

      if (connection.getResponseCode >= 400)
        println("Failed downloading from " + url)
      else
        urlObject #> file !
    }
  }
}
