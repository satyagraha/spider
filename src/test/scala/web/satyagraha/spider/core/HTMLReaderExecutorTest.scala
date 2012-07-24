package web.satyagraha.spider.core

import java.io.ByteArrayInputStream

import org.hamcrest.CoreMatchers.is
import org.junit.Assert.assertThat
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HTMLReaderExecutorTest extends FunSpec with ShouldMatchers {

  it("should decode plain text content") {
    val htmlReaderExecutor = new HTMLReaderExecutor
    val inputString = "sample content"
    val encoding = "latin1"
    println("input string: " + inputString)
    val inputStream = new ByteArrayInputStream(inputString.getBytes(encoding))
    val outputString = htmlReaderExecutor.tryConvert(inputStream, encoding)
    assertThat(outputString, is(inputString))
  }

}