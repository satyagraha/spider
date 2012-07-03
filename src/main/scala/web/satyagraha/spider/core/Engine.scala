package web.satyagraha.spider.core
import java.net.URI
import scala.collection.mutable.HashSet
import scala.collection.mutable.Queue
import java.io.File
import grizzled.slf4j.Logger
import scala.xml.Node
import net.liftweb.actor.LiftActor
import net.liftweb.actor.LAFuture
import net.liftweb.actor.LAScheduler
import java.net.URISyntaxException
import dispatch.StatusCode
import org.scala_tools.subcut.inject.BindingModule
import org.scala_tools.subcut.inject.Injectable

/**
 * Traverse a web site, retrieving and validating all links.
 * 
 * The class operates as follows.
 *
 * @param rootUriRaw
 * @param rootHeaders
 * @param readerCount
 * @param bindingModule
 */

abstract class Engine(rootUriRaw: String, val rootHeaders: Map[String, String], val readerCount: Int)(implicit val bindingModule: BindingModule) extends Injectable {

  private val rootUri = if ((new URI(rootUriRaw)).getPath() == "") { rootUriRaw + "/" } else { rootUriRaw }

  /////////////////////////////////////////////////////////////////////////////

  private abstract class NamedActor(val name: String) extends LiftActor {
    logger.debug(name + ":")

    protected def handler(): PartialFunction[Any, Unit]

    override def messageHandler = {
      Thread.currentThread.setName(name)
      handler
    }
  }

  private case class Reference(val referrer: String, val uri: String) {

    override def toString = {
      referrer + " --> " + uri
    }

  }

  /////////////////////////////////////////////////////////////////////////////

  private val logger = Logger(getClass)

  private val htmlReader = inject[HTMLReader]

  private val linkFactory = inject[LinkFactory]

  private var allDone: LAFuture[Any] = _

  /////////////////////////////////////////////////////////////////////////////
  // public api

  /**
   * Commence traversal of web site.
   *
   * @return
   */

  def start(): LAFuture[Any] = {
    logger.debug("start: root: " + rootUri)
    if (allDone != null) {
      throw new IllegalStateException
    }
    val pair = ("", rootUri)
    allDone = referenceActor !< pair
    allDone
  }

  /**
   * Release all resources on completion of traversal.
   */

  def stop(): Unit = {
    if (allDone == null) {
      throw new IllegalStateException
    }
    LAScheduler.shutdown()
  }

  /**
   * Implement this method to handle good link.
   * @param uri
   * @param response
   */

  def handleSuccess(uri: String, response: HTMLReaderResult): Unit

  /**
   * Implement this method to handle bad link.
   * @param referrer
   * @param uri
   */

  def handleFailure(referrer: String, uri: String): Unit

  /////////////////////////////////////////////////////////////////////////////

  private val referenceActor: NamedActor = new NamedActor("referenceActor") {
    var activeReferences = 0

    override def handler = {
      case pair: Tuple2[String, String] => {
        logger.debug("referenceActor: received pair: " + pair)
        assert(activeReferences == 0)
        activeReferences += 1
        targetActor ! new Reference(pair._1, pair._2)
        // reply is sent later on completion
      }
      case pairs: Seq[Tuple2[String, String]] => {
        logger.debug("referenceActor: received pairs: " + pairs)
        assert(activeReferences > 0)
        pairs foreach { pair =>
          activeReferences += 1
          targetActor ! new Reference(pair._1, pair._2)
        }
      }
      case ref: Reference => {
        logger.debug("referenceActor: received ref: " + ref)
        assert(activeReferences > 0)
        activeReferences -= 1
        if (activeReferences == 0) {
          logger.debug("referenceActor: completed, satisfying future")
          allDone.satisfy()
        }
      }
      case unknown => {
        throw new IllegalArgumentException(unknown.toString)
      }
    }
  }

  private val targetActor: NamedActor = new NamedActor("targetActor") {
    val targets = new HashSet[String]

    override def handler = {
      case ref: Reference => {
        logger.debug("targetActor: received: " + ref)
        if (targets.contains(ref.uri)) {
          // release reference
          referenceActor ! ref
        } else {
          targets += ref.uri
          queuedActor ! ref
        }
      }
      case unknown => {
        throw new IllegalArgumentException(unknown.toString)
      }
    }
  }

  private val queuedActor: NamedActor = new NamedActor("queuedActor") {
    val readersPool = new Queue[NamedActor]
    readersPool ++= ((0 until readerCount) map { idx => readerActorMake(idx) })

    val refsPending = new Queue[Reference]();

    override def handler = {
      case ref: Reference => {
        logger.debug("queuedActor: received: " + ref)
        if (readersPool.nonEmpty) {
          val reader = readersPool.dequeue();
          reader ! ref
        } else {
          refsPending += ref
        }
      }
      case responder: NamedActor => {
        if (refsPending.nonEmpty) {
          responder ! refsPending.dequeue()
        } else {
          readersPool += responder
        }
      }
      case unknown => {
        throw new IllegalArgumentException(unknown.toString)
      }
    }
  }

  private def readerActorMake(idx: Int): NamedActor = {
    val readerId = "readerActor-%03d".format(idx)
    new NamedActor(readerId) {
      override def handler = {
        case ref: Reference => {
          logger.debug("readerActor: " + ref)
          val headers = if (ref.uri == rootUri) rootHeaders else Map.empty[String, String]
          val fetchResult = htmlReader.doGET(ref.uri, headers)
          logger.debug("readerActor: fetchResult: " + fetchResult)
          fetchResult match {
            case Left(StatusCode(code, contents)) => {
              logger.warn("GET status code: " + code + " on: " + ref.uri)
              logger.debug("contents: " + contents)
              failureActor ! ref
            }
            case Left(exception) => {
              logger.warn("GET exception: " + exception.toString() + " on: " + ref.uri)
              failureActor ! ref
            }
            case Right(response) => {
              val pair = (ref, response)
              successActor ! pair
            }
          }
          queuedActor ! this
        }
        case unknown => {
          throw new IllegalArgumentException(unknown.toString)
        }
      }
    }
  }

  private val successActor: NamedActor = new NamedActor("successActor") {
    override def handler = {
      case pair: Tuple2[Reference, HTMLReaderResult] => {
        logger.debug("successActor: received: " + pair)
        val (ref @ Reference(referrer, uri), response) = pair
        handleSuccess(uri, response)
        if (uri == rootUri || linkFactory.isChildUri(rootUri, uri)) {
          val links = linkFactory.extractLinks(uri, response)
          if (links.nonEmpty) {
            val cleanLinks = linkFactory.cleanLinks(uri, links)
            val linkPairs = cleanLinks.map((uri, _))
            logger.debug("successActor: enqueuing: " + linkPairs)
            // create new refs synchronously to avoid race hazard of prematurely ending
            referenceActor ! linkPairs
          }
        } else {
          logger.debug("successActor: ignoring non-child uri: " + uri)
        }
        // release reference
        referenceActor ! ref
      }
      case unknown => {
        throw new IllegalArgumentException(unknown.toString)
      }
    }
  }

  private val failureActor: NamedActor = new NamedActor("failureActor") {
    override def handler = {
      case ref: Reference => {
        logger.debug("failureActor: received: " + ref)
        handleFailure(ref.referrer, ref.uri)
        // release reference
        referenceActor ! ref
      }
      case unknown => {
        throw new IllegalArgumentException(unknown.toString)
      }
    }
  }

}
