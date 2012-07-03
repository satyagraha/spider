package web.satyagraha.spider.core
import scala.xml.Node
import org.scala_tools.subcut.inject.BindingModule
import org.scala_tools.subcut.inject.Injectable
import java.net.URL
import org.apache.commons.io.FilenameUtils
import java.net.URI
import java.net.URISyntaxException

/**
 * Provides facilities to parse out links from HTML pages.
 */

class LinkFactory(implicit val bindingModule: BindingModule) extends Injectable {

  private val htmlParser = inject[HTMLParser]

  /////////////////////////////////////////////////////////////////////////////
  // public api

  /**
   * Predicate to check if one url is a descendant of another.
   *
   * @param rootUri
   * @param uri
   * @return
   */

  def isChildUri(rootUri: String, uri: String) = {
    require(rootUri != null)
    require(uri != null)

    uri.startsWith(rootUri) && (uri.length > rootUri.length)
  }

  /**
   * Predicate to check if a URL is valid.
   *
   * @param url
   * @return
   */

  def isValidUrl(url: String): Boolean = {
    require(url != null)

    try {
      new URL(url)
    } catch {
      case e: Exception => {
        return false
      }
    }
    return true;
  }

  /**
   * Default set of extensions blocked.
   */
  var extensionsBlocked = "doc,jpg,mp3,mus,png,ppt,pdf".split(',').toSet

  /**
   * Predicate to check if an extension is blocked.
   *
   * @param uri
   * @return
   */

  def isBlockedExtension(uri: String): Boolean = {
    require(uri != null)

    val ext = FilenameUtils.getExtension(uri).toLowerCase
    return extensionsBlocked.contains(ext)
  }

  /**
   * Retrieve all valid web hyperlinks within a HTML page.
   *
   * @param pageUri
   * @param pageResponse
   * @return
   */

  def extractLinks(pageUri: String, pageResponse: HTMLReaderResult): Seq[String] = {
    require(pageUri != null)
    require(pageResponse != null)

    if (!isValidUrl(pageUri) || isBlockedExtension(pageUri)) {
      return Seq()
    }

    val parseTree = htmlParser.parse(pageResponse.body)
    return parseTree.getLinks.map(toText).filter(isWebLink)
  }

  /**
   * Make hyperlinks absolute and strip fragment part if present.
   * @param pageUri
   * @param childLinks
   * @return
   */

  def cleanLinks(pageUri: String, childLinks: Seq[String]): Seq[String] = {
    require(pageUri != null)
    require(childLinks != null)

    return childLinks.map(joinLink(pageUri, _)).map(excludeFragment)
  }

  /////////////////////////////////////////////////////////////////////////////
  // implementation methods

  private def toText(node: Node): String = {
    node.toString.trim.replace(" ", "%20")
  }

  private def isWebLink(link: String): Boolean = {
    val linkParsed = try {
      new URI(link);
    } catch {
      case e: URISyntaxException => {
        return false;
      }
    }
    linkParsed.getScheme match {
      case null => true
      case s if Set("http", "https").contains(s.toLowerCase) => true
      case _ => false
    }
  }

  private def joinLink(uri: String, link: String): String = {
    val baseURI = new URI(uri)
    return baseURI.resolve(link).toString
  }

  private def excludeFragment(uri: String): String = {
    val hashPos = uri.indexOf('#')
    return if (hashPos != -1) { uri.substring(0, hashPos) } else { uri }
  }

}