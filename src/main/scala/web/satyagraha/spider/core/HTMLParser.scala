package web.satyagraha.spider.core

import scala.xml.{ Elem, XML, NodeSeq }
import scala.xml.factory.XMLLoader

import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

/**
 * Parser for HTML pages.
 */

class HTMLParser {

  /**
   * Parser result class.
   */

  class ParseTree(val top: Elem) {

    /**
     * Get all the link elements.
     *
     * @return
     */

    def getLinks(): NodeSeq = {
      top \\ "a" \\ "@href"
    }
  }

  private val factory = new SAXFactoryImpl()

  /**
   * Generate a parse tree.
   * @param html
   * @return
   */

  def parse(html: String): ParseTree = {
    require(html != null)
    val loader = XML.withSAXParser(factory.newSAXParser())
    val top = loader.loadString(html)
    return new ParseTree(top)
  }
}