package com.swantosaurus.catlog

import com.swantosaurus.catlog.LogOutput
import com.swantosaurus.catlog.LogMessage
import scala.util.matching.Regex

object LogParser {
  val logLinePattern: Regex = """([A-Z])\/([a-zA-Z0-9_-]+) *\( *([0-9]+)\): ([a-zA-Z_ 0-9']+)""".r

  def parse(line: String): LogOutput = {
    if(line == "- waiting for device -") {
      return ParseError(line)
    }
    logLinePattern.findFirstMatchIn(line) match {
      case Some(m) =>
        LogMessage(
            level = m.group(1),
            tag = m.group(2),
            processId = m.group(3).toInt,
            message = m.group(4)
        )
      case None => IgnoreOutput
    }
  }
}

