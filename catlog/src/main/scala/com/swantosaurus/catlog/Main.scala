package com.swantosaurus.catlog

import com.swantosaurus.catlog._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.Breaks._
import scala.concurrent.Future

import com.swantosaurus.commandLinePrinter.ColorPrinter
import java.io.PrintWriter
import akka.Done
import com.swantosaurus.catlog.CommandHolder

import org.jline.terminal.TerminalBuilder
import org.jline.terminal.Terminal
import org.jline.reader.LineReaderBuilder
import org.jline.reader.LineReader
import org.jline.utils.InfoCmp
import scala.runtime.stdLibPatches.language.future

/**
  * Main class to start the program
  * by creating this class the logcat will start and wait for user input
  */
class Program { 
  private val terminal = TerminalBuilder.builder().build()
  private val reader = LineReaderBuilder.builder().terminal(terminal).build()
  private var programStateHolder = ProgramStateHolder()
  private val stream = AdbLogcatStream()
  private val colorPrinter = ColorPrinter() 
  private val packageReader = ProcessReader()
  private var printingThread : Option[Future[Done]] = None
  private var pid : Option[Int] = None

  private val commands = CatlogCommands(
    stream,
    reader,
    packageReader,
    programStateHolder,
    (newPid) => pid = newPid,
    () => pid,
    printHelp,
    startLogging
  )

  
  printHelp()
  setupInput() 

  def printHelp() : Unit = {
    terminal.puts(InfoCmp.Capability.clear_screen)
    terminal.writer().println("Logcat help")
    terminal.writer().println("This program is used to view and filter logcat output from an android device")
    terminal.writer().println("Available commands:")
    terminal.writer().println(commands.get.getHelpString())
  }

  def startLogging() : Unit = {
    printingThread = Some(setupTerminalLoggingThread(terminal, stream, reader, ColorPrinter()))
  }

  def setupInput() : Unit = {
      while(programStateHolder.get() == ProgramState.Running) {
        val line = reader.readLine("logcat > ")
        handleUserInput(line)
      }
  }
  
  /**
      * Setup the terminal logging in new thread
      *
      * @param terminal
      * @param stream
      * @param reader
      * @param printer
      * @return Future[Done] of collecting the logcat stream 
      */
  private def setupTerminalLoggingThread(
    terminal: Terminal,
    stream: AdbLogcatStream,
    reader: LineReader,
    printer: ColorPrinter
  ): Future[Done] = {
    //terminal.puts(InfoCmp.Capability.clear_screen)
    val source = stream.logcatSource().runForeach(logOutput => {
      logOutput match {
        case ParseError(line) => { 
          printError(line)
        }
        case message: LogMessage => { 
          val processName = packageReader.getProcessPackageName(message.processId)
          if(processName.isDefined){
            if(pid.isDefined && pid.get == message.processId) {
              printLog(message)
            } else if(pid.isEmpty) {
              printLog(message)
            }
          }
        }
        case IgnoreOutput => {}
      }
    })
    return source
  }
  extension(s: String) {
    def colorizeType(): String = {
      val padString = fansi.Color.Black(s" ${s} ").render
      s match {
        case  "W" => fansi.Back.Yellow(padString).render
        case "E" => fansi.Back.Red(padString).render
        case "I" => fansi.Back.Green(padString).render
        case "D" => fansi.Back.Blue(padString).render
        case _ => fansi.Back.White(padString).render
      }
    } 
  }

  
  def handleUserInput(input: String): Unit = {
    commands.get.run(input)
  }
  
  extension (w: PrintWriter) {
    def handleError(line: String): Unit = {
      val colorPrinter = ColorPrinter()
      w.println(colorPrinter.colorRed("!!! Error detected, stopping catlog following line should provide more information!!"))
      w.println(line)
      w.println("type \"start\" to start catlog again")
    }
  }
  
  def printLog(message: LogMessage): Unit = {
    val processName = packageReader.getProcessPackageName(message.processId)
    reader.callWidget(LineReader.CLEAR)
    terminal.writer().println(
      colorPrinter.colorYellow(processName.getOrElse(" ").split('.').last.take(30).padLeft(30, ' ')) + " " +
      message.tag.take(20).padTo(20, ' ') + " " + 
      message.level.colorizeType() + " " + message.message
    )

    resetReaderLine()
  }
  
  def printError(errorLine: String): Unit = {
    terminal.writer().handleError(errorLine)

    stream.stopLogcat()
    resetReaderLine()
  }
  
  def resetReaderLine(): Unit = {
    reader.callWidget(LineReader.CLEAR)
    reader.callWidget(LineReader.REDRAW_LINE)
    reader.callWidget(LineReader.REDISPLAY)
    terminal.flush()
  }
}



@main 
def main = {
  val program = new Program()
}
