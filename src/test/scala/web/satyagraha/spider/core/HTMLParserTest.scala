package web.satyagraha.spider.core

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scala_tools.subcut.inject.BindingModule
import org.scala_tools.subcut.inject.Injectable
import org.scalatest.matchers.ShouldMatchers
import web.satyagraha.spider.Bindings

@RunWith(classOf[JUnitRunner])
class HTMLParserTest extends FunSpec with ShouldMatchers with Injectable {

  override def bindingModule = Bindings

  val htmlParser = inject[HTMLParser]

  describe("A HTMLParser") {

    it("should reject null argument") {
      intercept[IllegalArgumentException] {
        htmlParser.parse(null);
      }
    }

    it("should accept empty string") {
      val html = ""
      val parseTree = htmlParser.parse(html);
      val hrefNodes = parseTree.getLinks();
      hrefNodes should have length (0)
    }
    
    it("should accept malformed html") {
      val html = "<html>"
      val parseTree = htmlParser.parse(html);
      val hrefNodes = parseTree.getLinks();
      hrefNodes should have length (0)
    }
    
    it("should not find anchor") {
      val html = """<html><a name="abc">content</a></html>"""
      val parseTree = htmlParser.parse(html);
      val hrefNodes = parseTree.getLinks();
      hrefNodes should have length (0)
    }

    it("should find no href") {
      val html = """<html></html>"""
      val parseTree = htmlParser.parse(html);
      val hrefNodes = parseTree.getLinks();
      hrefNodes should have length (0)
    }
    
    it("should find one href") {
      val html = """<html><a href="abc">link</a></html>"""
      val parseTree = htmlParser.parse(html);
      val hrefNodes = parseTree.getLinks();
      hrefNodes should have length (1)
      val hrefs = hrefNodes.map(_.toString)
      hrefs should equal (Seq("abc"))
    }

    it("should find two hrefs") {
      val html = """<html><a href="abc">link</a><a href="def">link</a></html>"""
      val parseTree = htmlParser.parse(html);
      val hrefNodes = parseTree.getLinks();
      hrefNodes should have length (2)
      val hrefs = hrefNodes.map(_.toString)
      hrefs should equal (Seq("abc", "def"))
    }
    
  }
}