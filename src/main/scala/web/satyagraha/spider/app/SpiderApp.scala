package web.satyagraha.spider.app

import grizzled.slf4j.Logger
import scala.collection.mutable.HashMap
import org.slf4j.LoggerFactory
import web.satyagraha.spider.core.Engine
import web.satyagraha.spider.core.EngineHandler
import web.satyagraha.spider.Bindings

/**
 * Main program for the Spider app.
 */

object SpiderApp extends SpiderAppArgs {

  private val logger = Logger(SpiderApp.getClass)

  /**
   * Command line entry point.
   * 
   * @param args
   */
  
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      usage
      return
    }
    parseArgs(args.toList)
    if (urls.size != 1) {
      usage
      return
    }
    val url = urls(0)
    logger.info("main: processing " + url)
    val headers = new HashMap[String, String]()
    if (cookies.isDefined) {
      headers += ("Cookie" -> cookies.get)
    }
    if (logLevel.isDefined) {
      LoggerFactory.getLogger(Logger.RootLoggerName)
        .asInstanceOf[ch.qos.logback.classic.Logger]
        .setLevel(ch.qos.logback.classic.Level.toLevel(logLevel.get))
    }
    val readerCount = if (readers.isDefined) { readers.get } else 10

    implicit val bindingModule = Bindings
    val engine = new Engine(url, Map(headers.toList: _*), readerCount) with EngineHandler
    val future = engine.start
    future.get
    engine.stop()

    logger.info("main: processing complete")
    logger.info("main: pages read successfully: " + engine.successes.size)
    engine.successes foreach { success =>
//      logger.info("main: good link: " + success)
      println("main: good link: " + success)
    }
    logger.info("main: pages read unsuccessfully: " + engine.failures.size)
    engine.failures foreach { failure =>
//      logger.info("main: broken link: " + failure)
      println("main: broken link: " + failure)
    }
  }
}