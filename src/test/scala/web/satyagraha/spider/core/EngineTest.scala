package web.satyagraha.spider.core

import org.junit.runner.RunWith
import scala.collection.mutable.Queue
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scala_tools.subcut.inject.BindingModule
import org.scala_tools.subcut.inject.Injectable
import org.scalatest.matchers.ShouldMatchers
import web.satyagraha.spider.Bindings
import org.scala_tools.subcut.inject.NewBindingModule
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => the, any}
import org.scalatest.BeforeAndAfter

@RunWith(classOf[JUnitRunner])
class EngineTest extends FunSpec with BeforeAndAfter with ShouldMatchers with Injectable {

  implicit var bindingModule : BindingModule = _
  
  var htmlReader : HTMLReader = _
  
  before {
    
    htmlReader = MockitoSugar.mock[HTMLReader]
    
    bindingModule = new NewBindingModule({ implicit module =>
    
      import module._  // optional but convenient
    
      bind [HTMLReader] toSingle htmlReader
      
      bind [HTMLParser] toSingle new HTMLParser
      
      bind [LinkFactory] toSingle new LinkFactory
    
    })
    
  }

  describe("An Engine") {

    it ("should identify good links") {
      
      val rootUri = "http://hostname/"
      val headers = Map.empty[String, String]
      val readerCount = 1
      
      val rootHtmlBody = """<html><a href="link">text</a></html>""" 
      val rootReadResult = Right(new HTMLReaderResult(Map(), rootHtmlBody))
      when(htmlReader.doGET(rootUri, headers)).thenReturn(rootReadResult)
      
      val linkUri = rootUri + "link"
      val linkHtmlBody = """<html></html>"""
      val linkReadResult = Right(new HTMLReaderResult(Map(), linkHtmlBody))
      when(htmlReader.doGET(linkUri, headers)).thenReturn(linkReadResult)
      
      val engine = new Engine(rootUri, headers, readerCount) with EngineHandler
      
      val future = engine.start
      future.get
      
      engine.successes should equal (Queue(rootUri, linkUri))
      engine.failures should equal (Queue())

    }
   
    it ("should identify bad links") {
        
      val rootUri = "http://hostname/"
      val headers = Map.empty[String, String]
      val readerCount = 1
                        
      val rootHtmlBody = """<html><a href="link">text</a></html>""" 
      val rootReadResult = Right(new HTMLReaderResult(Map(), rootHtmlBody))
      when(htmlReader.doGET(rootUri, headers)).thenReturn(rootReadResult)
                        
      val linkUri = rootUri + "link"
      val linkException = new RuntimeException
      val linkReadResult = Left(linkException)
      when(htmlReader.doGET(linkUri, headers)).thenReturn(linkReadResult)
                        
      val engine = new Engine(rootUri, headers, readerCount) with EngineHandler
                       
      val future = engine.start
      future.get
                       
      engine.successes should equal (Queue(rootUri))
      engine.failures should equal (Queue(engine getFailureRepr(rootUri, linkUri)))
        
    }
    
  }
}