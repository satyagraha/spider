package web.satyagraha.spider.core

import dispatch._
import grizzled.slf4j.Logger
import org.scala_tools.subcut.inject.Injectable
import org.scala_tools.subcut.inject.BindingModule
import java.io.InputStream
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset
import java.nio.ByteBuffer
import java.nio.charset.MalformedInputException

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
   * Convert an input stream to string.
   * 
   * @param stream
   * @param charsetNames
   * @return
   */
  def tryConvert(stream: InputStream, charsetNames: String*): String = {
    val bytes = IOUtils.toByteArray(stream)
    println("got bytes: " + bytes.length)
    charsetNames foreach { charsetName =>
      try {
        val decoder = Charset.forName(charsetName).newDecoder
        return decoder.decode(ByteBuffer.wrap(bytes)).toString
      } catch {
        case e: MalformedInputException =>
      }
    }
    throw new MalformedInputException(0)
  }

  /**
   * Create a request using the providing url and headers.
   *
   * @param uri
   * @param requestHeaders
   * @return
   */
  def buildRequest(uri: String, requestHeaders: Map[String, String]): Request = {
    require(uri != null)
    require(requestHeaders != null)

    return new Request(uri) <:< requestHeaders
  }

  /**
   * Execute a request.
   *
   * @param request
   * @return
   */
  def fetchRequest(request: Request) = {
    apply(request >+ { req => (req >:> { map => map }, req >> { (stm, charset) => tryConvert(stm, charset, "latin1") }) })
  }

}

/**
 * High-level facade for web retrieval services.
 */

class HTMLReader(implicit val bindingModule: BindingModule) extends Injectable {

  val logger = Logger(getClass)

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
    logger.debug("requestHeaders: " + requestHeaders)

    val result = try {
      val request = executor.buildRequest(uri, requestHeaders)
      val (responseHeaders, body) = executor.fetchRequest(request)
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