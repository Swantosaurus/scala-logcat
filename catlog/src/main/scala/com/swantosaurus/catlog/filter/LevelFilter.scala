package com.swantosaurus.catlog.filter

import com.swantosaurus.catlog.model.{LogLevel, LogMessage}

class LevelFilter extends LogFilter {
  private var minLevel : LogLevel = LogLevel.DEBUG
  private var maxLevel : LogLevel = LogLevel.FATAL

  def setLevel(lower: LogLevel, upper: LogLevel) = {
    setMinLevel(lower)
    setMaxLevel(upper)
  }

  def setLevel(level: LogLevel) = {
    setMinLevel(level)
    setMaxLevel(level)
  }

  def setMinLevel(level: LogLevel) = {
    minLevel = level
  }
  
  def setMaxLevel(level: LogLevel) = {
    maxLevel = level
  }

  override def filter(log: Option[LogMessage]): Option[LogMessage] = {
    log match {
      case Some(log) => { if(log.getLevel() >= minLevel && log.getLevel() <= maxLevel) {
        Some(log)
      } else {
        //println("log level not in range " + log.getLevel() + " " + minLevel + " " + maxLevel)
        None
      }
      }
      case _ => {
        None
      }
    }
  }
}



