package com.swantosaurus.catlog

import com.swantosaurus.catlog._
import org.jline.terminal.Terminal
import org.jline.reader.LineReader
 
class CatlogCommands(
  stream: AdbLogcatStream,
  reader: LineReader, 
  packageReader: ProcessReader, 
  programStateHolder: ProgramStateHolder,
  setPid: Option[Int] => Unit,
  getPid: () => Option[Int],
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
        val packageName = reader.readLine("package: ")
        
        if(packageName == "") {
          println("clearing pid")
          setPid(None)
        } else {


        setPid(packageReader.getProcessPidByPartOfName(packageName))
        
        if(getPid().isEmpty){
          packageReader.readAdbPs()
          setPid(packageReader.getProcessPidByPartOfName(packageName))
        }

        if(getPid().isDefined) {
          println("using pid: " + getPid().get)
        } else {
          println("no pid found for package: " + packageName)
        }
        }
      }),
      Command("start", "start logging", () => {
        startLogging()
      }),
      Command("help", "print this help", () => printHelp())
    ),
    Command("unknown", "unknown command", () => println("Unknown command, type \"help\" to see available commands"))
  )
  def get = commandHolder
} 
