import sys.process._
import java.io.File
import java.net.URL

import scala.language.postfixOps

class Downloader(outputDir: Option[String], url: String) {
  def download(): Unit = {
    println(url)
    if (outputDir.isDefined) {
      val sanitizedPath = url
        .replaceAll("[\\\\/:*?\"<>|]", "_")

      val dir = new File(outputDir.get)
      if (!dir.exists()) {
        dir.mkdir()
      }

      val file = new File(dir, sanitizedPath)

      val urlObject = new URL(url)
      urlObject #> file !
    }
  }
}
