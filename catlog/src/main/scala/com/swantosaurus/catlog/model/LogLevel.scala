package com.swantosaurus.catlog.model

def LogLevelFromString(level: String): LogLevel = {
  level.toUpperCase match {
    case "S" => LogLevel.SILENT
    case "D" => LogLevel.DEBUG
    case "I" => LogLevel.INFO
    case "W" => LogLevel.WARN
    case "E" => LogLevel.ERROR
    case "V" => LogLevel.VERBOSE
    case "F" => LogLevel.FATAL
    case _   => LogLevel.UNKNOWN
  }
}

enum LogLevel {
  case SILENT, DEBUG, INFO, WARN, ERROR, VERBOSE, FATAL, UNKNOWN

  def compare(other: LogLevel): Int = {
    this.ordinal - other.ordinal
  }

  def >(other: LogLevel): Boolean = {
    compare(other) > 0
  }

  def >=(other: LogLevel): Boolean = {
    compare(other) >= 0
  }

  def <(other: LogLevel): Boolean = {
    compare(other) < 0
  }

  def <=(other: LogLevel): Boolean = {
    compare(other) <= 0
  }
}
