package web.satyagraha.spider.app

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.junit.JUnitRunner
import org.scala_tools.subcut.inject.BindingModule
import org.scala_tools.subcut.inject.Injectable
import org.scalatest.BeforeAndAfterAll
import org.scala_tools.subcut.inject.NewBindingModule
import web.satyagraha.spider.Bindings

@RunWith(classOf[JUnitRunner])
class SpiderAppArgsTest extends FunSpec with Injectable {
  
  override def bindingModule = Bindings
  
  val appArgs = inject[SpiderAppArgs]
 
  describe("A SpiderAppArgs") {
    
    it("should require a uri") {
      intercept[IllegalArgumentException] {
        appArgs.parseArgs(List())
      }
    }
    
    it("should parse one uri") {
      val uri = "uri"
      appArgs.parseArgs(List(uri))
      assert(appArgs.urls === Seq(uri))
    }
    
    it("should parse two uris") {
      val uri1 = "uri1"
      val uri2 = "uri2"
      appArgs.parseArgs(List(uri1, uri2))
      assert(appArgs.urls === Seq(uri1, uri2))
    }
    
    it("should parse cookies") {
      val cookie =  "my cookie"
      val uri = "uri"
      appArgs.parseArgs(List("--cookies", cookie, uri))
      assert(appArgs.cookies === Some(cookie))
      assert(appArgs.urls === Seq(uri))
    }
    
    it("should parse loglevel") {
      val logLevel =  "logLevel"
      val uri = "uri"
      appArgs.parseArgs(List("--loglevel", logLevel, uri))
      assert(appArgs.logLevel === Some(logLevel))
      assert(appArgs.urls === Seq(uri))
    }
    
    it("should parse readers") {
      val readers =  "99"
      val uri = "uri"
      appArgs.parseArgs(List("--readers", readers, uri))
      assert(appArgs.readers === Some(readers.toInt))
      assert(appArgs.urls === Seq(uri))
    }
    
    it("should reject an unknown option") {
      val uri = "uri"
      intercept[IllegalArgumentException] {
        appArgs.parseArgs(List("--unknown", uri))
      }
    }
    
  }

}