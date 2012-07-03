package web.satyagraha.spider

import org.scala_tools.subcut.inject.NewBindingModule
import web.satyagraha.spider.core.HTMLParser
import web.satyagraha.spider.app.SpiderAppArgs
import web.satyagraha.spider.core.HTMLReader
import web.satyagraha.spider.core.HTMLReaderExecutor
import web.satyagraha.spider.core.LinkFactory

/**
 * Provide the default set of subcut dependency injection bindings for the Spider app.
 * 
 */
object Bindings extends NewBindingModule({ implicit module =>
  
  import module._  // optional but convenient

  bind [HTMLReader] toSingle new HTMLReader
  
  bind [HTMLReaderExecutor] toSingle new HTMLReaderExecutor

  bind [HTMLParser] toSingle new HTMLParser
  
  bind [LinkFactory] toSingle new LinkFactory
  
  bind [SpiderAppArgs] toSingle new Object with SpiderAppArgs
  
}) 

