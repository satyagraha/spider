package web.satyagraha.spider.app
import scala.collection.mutable.HashMap

/**
 * Provide facility to parse Spider app command line arguments.
 */
trait SpiderAppArgs {

  var cookies: Option[String] = None
  var logLevel: Option[String] = None
  var readers: Option[Int] = None
  var urls: Seq[String] = Seq.empty[String]

  /**
   * Populate instance variables from command line arguments.
   * @param args
   */
  def parseArgs(args: List[String]): Unit = {
    require(args.nonEmpty)

    val arg = args(0);
    if (arg startsWith "-") {
      args match {
        case "--cookies" :: cookiesArg :: rest => {
          cookies = Some(cookiesArg)
          parseArgs(rest)
        }
        case "--loglevel" :: logLevelArg :: rest => {
          logLevel = Some(logLevelArg)
          parseArgs(rest)
        }
        case "--readers" :: readersArg :: rest => {
          readers = Some(readersArg.toInt)
          parseArgs(rest)
        }
        case _ => throw new IllegalArgumentException("Unexpected option: " + args)
      }
    } else {
      urls = args
    }
  }

  def usage(): Unit = {
    println("Usage:")
    println("  scala SpiderApp [--cookies cookies] [--loglevel loglevel] [--readers readers] url")
  }
}