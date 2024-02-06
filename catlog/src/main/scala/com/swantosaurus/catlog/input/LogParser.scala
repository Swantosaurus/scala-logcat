package com.swantosaurus.catlog.input

import com.swantosaurus.catlog.model.{IgnoreOutput, LogMessage, LogOutput, ParseError}

import scala.util.matching.Regex

object LogParser {
private val logLinePattern: Regex = """([A-Z])\/([a-zA-Z0-9_-]+) *\( *([0-9]+)\): (.+)""".r

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

