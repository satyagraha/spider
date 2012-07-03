package web.satyagraha.spider.core

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scala_tools.subcut.inject.BindingModule
import org.scala_tools.subcut.inject.Injectable
import org.scalatest.matchers.ShouldMatchers
import web.satyagraha.spider.Bindings
import org.scala_tools.subcut.inject.NewBindingModule
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => the, any}
import dispatch.Handler
import java.net.UnknownHostException

@RunWith(classOf[JUnitRunner])
class HTMLReaderTest extends FunSpec with BeforeAndAfter with ShouldMatchers /*with MockitoSugar*/ with Injectable {

  implicit var bindingModule : BindingModule = _

  var htmlReader : HTMLReader = _
  var htmlReaderExecutor : HTMLReaderExecutor = _
  
  before {
    
    htmlReaderExecutor = MockitoSugar.mock[HTMLReaderExecutor]
    
    bindingModule = new NewBindingModule({ implicit module =>
    
      import module._  // optional but convenient
      
      bind [HTMLReader] toSingle new HTMLReader
    
      bind [HTMLReaderExecutor] toSingle htmlReaderExecutor 
      
    })
      
    htmlReader = inject[HTMLReader]
    
  }

  describe("A HTMLReader") {

    it("should reject null argument 1") {
      intercept[IllegalArgumentException] {
        htmlReader.doGET(null, Map());
      }
    }
    
    it("should reject null argument 2") {
        intercept[IllegalArgumentException] {
            htmlReader.doGET("", null);
        }
    }

    it("should fetch the uri passing the cookies and return a good response") {
      val uri = "http://www.abc.com"
      val requestHeaders = Map("session" -> "12345")
      val content = "<html>Hi!</html>"
      val responseHeaders = Map("info" -> Set("a", "b"))
      when(htmlReaderExecutor.fetch(uri, requestHeaders)).thenReturn((responseHeaders, content))
      val result = htmlReader.doGET(uri, requestHeaders)
      verify(htmlReaderExecutor).fetch(the(uri), the(requestHeaders))
      verifyNoMoreInteractions(htmlReaderExecutor)
      result should be (Right(HTMLReaderResult(responseHeaders, content)))
    }

    it("should fetch the uri passing the cookies and return a failure response") {
      val uri = "http://www.abc.com"
      val requestHeaders = Map("session" -> "12345")
      val content = "<html>Hi!</html>"
      val responseHeaders = Map("info" -> Set("a", "b"))
      val exception = new RuntimeException
      when(htmlReaderExecutor.fetch(uri, requestHeaders)).thenThrow(exception)
      val result = htmlReader.doGET(uri, requestHeaders)
      verify(htmlReaderExecutor).fetch(the(uri), the(requestHeaders))
      verifyNoMoreInteractions(htmlReaderExecutor)
      result should be (Left(exception))
    }
  }
  
}