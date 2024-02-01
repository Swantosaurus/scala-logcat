package com.swantosaurus.catlog

import com.swantosaurus.commandLinePrinter.ColorPrinter  

trait LogOutput

case class LogMessage(
  level: String,
  tag: String,
  processId: Int,
  message: String
) extends LogOutput 

case class ParseError(message: String) extends LogOutput 

object IgnoreOutput extends LogOutput
