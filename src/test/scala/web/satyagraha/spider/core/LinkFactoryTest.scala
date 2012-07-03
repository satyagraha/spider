package web.satyagraha.spider.core

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scala_tools.subcut.inject.BindingModule
import org.scala_tools.subcut.inject.Injectable
import org.scalatest.matchers.ShouldMatchers
import web.satyagraha.spider.Bindings

@RunWith(classOf[JUnitRunner])
class LinkFactoryTest extends FunSpec with ShouldMatchers with Injectable {

  override def bindingModule = Bindings

  val linkFactory = inject[LinkFactory]

  describe("A LinkFactory") {

    it ("should identify child uri's") {

      intercept[IllegalArgumentException] {
        linkFactory.isChildUri(null, "");
      }

      intercept[IllegalArgumentException] {
          linkFactory.isChildUri("", null);
      }
      
      linkFactory.isChildUri("http://parent", "http://parent/child") should be (true)
      
      linkFactory.isChildUri("http://parent/child", "http://parent") should be (false)
      
      linkFactory.isChildUri("http://parent", "http://parent") should be (false) 
    }
    
    it("should validate url's") {
      
      intercept[IllegalArgumentException] {
        linkFactory.isValidUrl(null);
      }
      
      linkFactory.isValidUrl("http://parent") should be (true)
      
      linkFactory.isValidUrl("https://parent") should be (true)
      
      linkFactory.isValidUrl("ftp://parent") should be (true)
      
      linkFactory.isValidUrl("xyz://parent") should be (false)
      
      linkFactory.isValidUrl("parent") should be (false)
      
      linkFactory.isValidUrl("") should be (false)
      
    }

    it("should validate extensions") {

      intercept[IllegalArgumentException] {
        linkFactory.isBlockedExtension(null);
      }
      
      linkFactory.isBlockedExtension("http://parent") should be (false)
      
      linkFactory.isBlockedExtension("http://parent.") should be (false)
      
      linkFactory.isBlockedExtension("http://parent.htm") should be (false)
      
      linkFactory.isBlockedExtension("http://parent.html") should be (false)
      
      linkFactory.isBlockedExtension("http://parent.doc") should be (true)
      
      linkFactory.isBlockedExtension("http://parent.pdf") should be (true)
      
    }
    
    it("should extract web links") {
      
      intercept[IllegalArgumentException] {
        linkFactory.extractLinks(null, new HTMLReaderResult(null, null))
      }

      intercept[IllegalArgumentException] {
          linkFactory.extractLinks("", null)
      }
      
      val uri = "http://local/"
       
      {
        val r = new HTMLReaderResult(Map(), """<html></html>""")
        val l = linkFactory.extractLinks(uri, r)
        l should equal (Seq())
      }
      
      {
          val r = new HTMLReaderResult(Map(), """<html><a href="link">text</a></html>""")
          val l = linkFactory.extractLinks(uri, r)
          l should equal (Seq("link"))
      }
      
      {
        val r = new HTMLReaderResult(Map(), """<html><a href="http://external/link">text</a></html>""")
        val l = linkFactory.extractLinks(uri, r)
        l should equal (Seq("http://external/link"))
      }
      
      {
          val r = new HTMLReaderResult(Map(), """<html><a href="https://external/link">text</a></html>""")
          val l = linkFactory.extractLinks(uri, r)
          l should equal (Seq("https://external/link"))
      }
      
      {
          val r = new HTMLReaderResult(Map(), """<html><a href="email://some.body@some.where">text</a></html>""")
          val l = linkFactory.extractLinks(uri, r)
          l should equal (Seq())
      }
      
    }
    
    it("should clean web links") {
      
      intercept[IllegalArgumentException] {
        linkFactory.cleanLinks(null, Seq())
      }

      intercept[IllegalArgumentException] {
          linkFactory.cleanLinks("", null)
      }
      
      val uri = "http://local/"
        
      {
        var l = linkFactory.cleanLinks(uri, Seq())
        l should equal (Seq())
      }

      {
        var l = linkFactory.cleanLinks(uri, Seq("abc"))
        l should equal (Seq(uri + "abc"))
      }
      
      {
        var l = linkFactory.cleanLinks(uri + "page.html", Seq("abc"))
        l should equal (Seq(uri + "abc"))
      }
      
      {
          var l = linkFactory.cleanLinks(uri, Seq("abc#fragment"))
          l should equal (Seq(uri + "abc"))
      }
      
    }
    
  }
}