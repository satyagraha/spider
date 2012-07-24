package web.satyagraha.spider.app
import web.satyagraha.spider.Bindings
import web.satyagraha.spider.core.HTMLReader

/**
 * Simple object to read a web page.
 */
object GetPage {

  def fetch(url : String) : Unit = {
    implicit val bindingModule = Bindings
    
    val cookies = Map.empty[String, String]
    val reader = new HTMLReader
    val result = reader.doGET(url, cookies)
    result match {
      case Left(exception) =>
        exception.printStackTrace()
      case Right(response) =>
        println(response)
    }
    
  }
  
  def main(args: Array[String]): Unit = {
    fetch(args(0))
  }

}