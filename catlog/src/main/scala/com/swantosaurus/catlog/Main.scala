package com.swantosaurus.catlog

import com.swantosaurus.catlog.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.Breaks.*
import scala.concurrent.Future
import com.swantosaurus.commandLinePrinter.ColorPrinter

import java.io.PrintWriter
import akka.Done
import com.swantosaurus.catlog.commands.{CatlogCommands, CommandHolder}
import com.swantosaurus.catlog.filter.{LevelFilter, LogFilter, ProcessFilter}
import com.swantosaurus.catlog.input.AdbLogcatStream
import com.swantosaurus.catlog.model.{IgnoreOutput, LogMessage, ParseError}
import com.swantosaurus.catlog.utils.ProcessReader
import org.jline.terminal.TerminalBuilder
import org.jline.terminal.Terminal
import org.jline.reader.LineReaderBuilder
import org.jline.reader.LineReader
import org.jline.utils.InfoCmp

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.Materializer

import scala.runtime.stdLibPatches.language.future

implicit val sys : ActorSystem = ActorSystem()
implicit val materializer : Materializer = Materializer(sys)

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
  private val filters = List[LogFilter](
    new ProcessFilter(packageReader),
    new LevelFilter(),
  )

  private val commands = CatlogCommands(
    stream,
    reader,
    packageReader,
    programStateHolder,
    filters,
    printHelp,
    startLogging
  )

  
  printHelp()
  setupInput() 

  private def printHelp() : Unit = {
    terminal.puts(InfoCmp.Capability.clear_screen)
    terminal.writer().println("Logcat help")
    terminal.writer().println("This program is used to view and filter logcat output from an android device")
    terminal.writer().println("Available commands:")
    terminal.writer().println(commands.get.getHelpString())
  }

  private def startLogging() : Unit = {
    printingThread = Some(setupTerminalLoggingThread(terminal, stream, reader, ColorPrinter()))
  }

  private def setupInput() : Unit = {
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
          filters.foldLeft(Option(message))((log, filter) => filter.filter(log)) match {
            case Some(log) => printLog(log)
            case None => {}
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
        case "W" => fansi.Back.Magenta(padString).render
        case "E" => fansi.Back.Red(padString).render
        case "I" => fansi.Back.Cyan(padString).render
        case "D" => fansi.Back.LightBlue(padString).render
        case _ => fansi.Back.White(padString).render
      }
    } 
  }

  
  private def handleUserInput(input: String): Unit = {
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
  
  private def printLog(message: LogMessage): Unit = {
    val processName = packageReader.getProcessPackageName(message.processId)
    reader.callWidget(LineReader.CLEAR) 
    val packageSize = 15
    val tagSize = 15
    val levelSize = 3
    val headerSize = packageSize + 1 + tagSize + 1 + levelSize + 1
    val windowSize = terminal.getWidth() - headerSize
    val messageSize = message.message.length()
    var written = 0

    terminal.writer().println(
      colorPrinter.colorYellow(processName.getOrElse(" ").split('.').last.take(packageSize).padLeft(packageSize, ' ')) + " " +
      colorPrinter.colorBlue(message.tag.take(tagSize).padTo(tagSize, ' ')) + " " + 
      message.level.colorizeType() + " " + message.message.take(windowSize)
    )
    written += windowSize
    while (written < messageSize) {
      terminal.writer().println(" " * headerSize + message.message.slice(written, written + windowSize))
      written += windowSize
    }


    resetReaderLine()
  }
  
  private def printError(errorLine: String): Unit = {
    terminal.writer().handleError(errorLine)

    stream.stopLogcat()
    resetReaderLine()
  }
  
  private def resetReaderLine(): Unit = {
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
