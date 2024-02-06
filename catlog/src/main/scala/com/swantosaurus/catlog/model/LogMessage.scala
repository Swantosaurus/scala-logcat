package com.swantosaurus.catlog.model

trait LogOutput

case class LogMessage(
  level: String,
  tag: String,
  processId: Int,
  message: String
) extends LogOutput {
  def getLevel(): LogLevel = {
    LogLevelFromString(level)
  }
}

case class ParseError(message: String) extends LogOutput 

object IgnoreOutput extends LogOutput
