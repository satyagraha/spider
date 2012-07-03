package web.satyagraha.spider.core

import dispatch._
import grizzled.slf4j.Logger
import org.scala_tools.subcut.inject.Injectable
import org.scala_tools.subcut.inject.BindingModule

/**
 * Provide container for web reader results.
 */

case class HTMLReaderResult(val headers: Map[String, Set[String]], val body: String)

/**
 * Provide slf4j logging adapter for Dispatch.
 */

trait HTMLReaderLogging extends RequestLogging {

  def logger: Logger

  override def make_logger = new dispatch.Logger {

    override def info(msg: String, items: Any*) = logger.info(msg.format(items: _*))

    override def warn(msg: String, items: Any*) = logger.warn(msg.format(items: _*))

  }
}

/**
 * Assembly of Dispatch components providing web retrieval service.
 */

class HTMLReaderExecutor extends Http with thread.Safety with HttpExecutor with HTMLReaderLogging {
  val logger = Logger(getClass)

  /**
   * Retrieve the url using provided headers.
   *
   * @param uri
   * @param requestHeaders
   * @return
   */

  def fetch(uri: String, requestHeaders: Map[String, String]) = {
    val request: Request = new Request(uri) <:< requestHeaders
    apply(request >+ { req => (req >:> { map => map }, req >- { str => str }) })
  }
}

/**
 * High-level facade for web retrieval services.
 */

class HTMLReader(implicit val bindingModule: BindingModule) extends Injectable {

  private val executor = inject[HTMLReaderExecutor]

  /**
   * Perform HTTP GET to retrieve web page.
   * 
   * @param uri
   * @param requestHeaders
   * @return
   */

  def doGET(uri: String, requestHeaders: Map[String, String]): Either[Exception, HTMLReaderResult] = {
    require(uri != null)
    require(requestHeaders != null)

    val result = try {
      val (responseHeaders, body) = executor.fetch(uri, requestHeaders)
      Right(new HTMLReaderResult(responseHeaders, body))
    } catch {
      case e: Exception => {
        Left(e)
      }
    }
    //executor.shutdown()
    result
  }

}