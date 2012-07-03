package web.satyagraha.spider.core
import scala.collection.mutable.Queue
import grizzled.slf4j.Logger

/**
 * Provides a simple implementation of Spider app link result handling.
 */
trait EngineHandler {

  private val logger = Logger(getClass)
    
  val successes = new Queue[String]

  def handleSuccess(uri: String, response: HTMLReaderResult): Unit = {
    logger.debug("handleSuccess: body length: " + response.body.length())
    successes += uri
  }
  
  val failures = new Queue[String]
  
  def handleFailure(referrer: String, uri: String): Unit = {
    val failureRepr = getFailureRepr(referrer, uri)
    logger.debug("handleFailure: " + failureRepr)
    failures += failureRepr
  }
  
  def getFailureRepr(referrer: String, uri: String) = referrer + " ---> " + uri 

}