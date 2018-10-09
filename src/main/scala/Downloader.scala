import sys.process._
import java.io.File
import java.net.URL

import scala.language.postfixOps

/*
A class to help download a file from a URL.
The object of this class is constructed with the output folder where the downloaded pages need to resize
 */
class Downloader(outputDir: Option[String]) {
  // Create the output folder if it doesn't exist.
  private val dir = new File(outputDir.get)
  if (!dir.exists()) {
    dir.mkdir()
  }

  def download(url: String): Unit = {
    // Print the URL that is being downloaded
    println(url)

    if (outputDir.isDefined) {
      // Files are downloaded with same names as the URLs.
      // But certain characters cannot be saved as filename.
      // So sanitize the filename by replacing invalid characters with underscore.
      val sanitizedPath = url
        .replaceAll("[\\\\/:*?\"<>|]", "_")
      val file = new File(dir, sanitizedPath)

      // Next, download the file.
      val urlObject = new URL(url)
      urlObject #> file !
    }
  }
}
