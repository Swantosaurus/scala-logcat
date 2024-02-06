package com.swantosaurus.catlog.commands

import com.swantosaurus.catlog.*
import com.swantosaurus.catlog.commands.{Command, CommandHolder}
import com.swantosaurus.catlog.filter.{LevelFilter, LogFilter, ProcessFilter}
import com.swantosaurus.catlog.input.AdbLogcatStream
import com.swantosaurus.catlog.model.{LogLevel, LogLevelFromString}
import com.swantosaurus.catlog.utils.ProcessReader
import org.jline.reader.LineReader
 
class CatlogCommands(
  stream: AdbLogcatStream,
  reader: LineReader, 
  packageReader: ProcessReader, 
  programStateHolder: ProgramStateHolder,
  filters: List[LogFilter],
  printHelp: () => Unit,
  startLogging: () => Unit, 
) {
  val commandHolder = CommandHolder(
    List(
      Command("q", "quit", () => {
        stream.stopLogcat()
        programStateHolder.setState(ProgramState.Stopped)
      }),
      Command("f", "filter", () => {
        val filter = reader.readLine("filter: ")
        stream.addFilter(filter)
      }),
      Command("stop", "stop logging", () =>  stream.stopLogcat()),
      Command("p" , "filters processes by packageName", () => {
        val processFilter = filters.find(_.isInstanceOf[ProcessFilter]).get.asInstanceOf[ProcessFilter]
        val packageName = reader.readLine("package: ")
        
        if(packageName == "") {
          println("clearing pid")
          processFilter.setPid(None)
        } else {
          processFilter.setPid(packageReader.getProcessPidByPartOfName(packageName))



          if(processFilter.pid.isEmpty){
            packageReader.readAdbPs()
            processFilter.setPid(packageReader.getProcessPidByPartOfName(packageName))
          }

          if(processFilter.pid.isDefined) {
            println("using pid: " + processFilter.pid.get)
          } else {
            println("no pid found for package: " + packageName)
          }
        }
      }),
      Command("start", "start logging", () => {
        startLogging()
      }),
      Command("help", "print this help", () => printHelp()),
      Command("l", "set logging Level (possible setters E(ErrorOnly), W-E(Warning to Error), W+(Warning and more savere than this)", () => {
        var level = reader.readLine("level: ")
        level = level.trim().toUpperCase()
        if(level.length() == 1) {
          val logLevel = LogLevelFromString(level)
          if(logLevel == LogLevel.UNKNOWN) {
            println("Unknown level: " + level)
          } else {
            filters.find(_.isInstanceOf[LevelFilter]) match {
              case Some(filter) => filter.asInstanceOf[LevelFilter].setLevel(logLevel)
              case None => {}
            }
          }
        } else if (level.length() > 1) {
          val lowerLevel = LogLevelFromString(level.substring(0, 1))
          if(lowerLevel == LogLevel.UNKNOWN){
            println("Unknown level: " + level.substring(0, 1))
          } else {
            val separator = level.substring(1, 2)
            if (separator == "+") {
              filters.find(_.isInstanceOf[LevelFilter]) match {
                case Some(filter) => filter.asInstanceOf[LevelFilter].setLevel(lowerLevel, LogLevel.VERBOSE)
                case None => {}
              }
            } else if (separator != "-" ) {
              val upperLevel = LogLevelFromString(level.substring(2))
              if (upperLevel == LogLevel.UNKNOWN) {
                println("Unknown level: " + level.substring(2))
              } else {
                filters.find(_.isInstanceOf[LevelFilter]) match {
                  case Some(filter) => filter.asInstanceOf[LevelFilter].setLevel(lowerLevel, upperLevel)
                  case None => {}
                }
              }
            }
          }
        } else {
          println("Unknown level: " + level)
        }
      }
    )),
    Command("unknown", "unknown command", () => println("Unknown command, type \"help\" to see available commands"))
  )
  def get = commandHolder
} 
