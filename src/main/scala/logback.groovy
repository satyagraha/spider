
// Logging configuration in groovy is documented at: http://logback.qos.ch/manual/groovy.html

import static ch.qos.logback.classic.Level.*
import ch.qos.logback.classic.encoder.*
import ch.qos.logback.classic.filter.*
import ch.qos.logback.core.*
import ch.qos.logback.core.status.OnConsoleStatusListener

statusListener(OnConsoleStatusListener)

def stdoutKey = 'STDOUT'
appender(stdoutKey, ConsoleAppender) {
  filter(ThresholdFilter) {
    level = TRACE
  }
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  }
}

root(INFO, [stdoutKey])
