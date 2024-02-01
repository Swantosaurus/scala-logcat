package com.swantosaurus.catlog

import com.swantosaurus.catlog.LogParser

import akka.actor.ActorSystem
import akka.stream.scaladsl.*
import akka.stream.*
import scala.sys.process.*
import org.jline.reader._
import org.jline.terminal._
import scala.concurrent.Future
import akka.Done
import scala.concurrent.Promise
import scala.concurrent.ExecutionContext
import java.util.UUID

implicit val system: ActorSystem = ActorSystem("AdbLogcatStream")
implicit val materializer: ActorMaterializer = ActorMaterializer()

class AdbLogcatStream { 
  implicit val executionContext: ExecutionContext = system.dispatcher
  val parser = LogParser
  var source : Option[Source[LogOutput, _]] = None
  val lastSnk = Sink.last[LogOutput]
  var killSwitch:Option[UniqueKillSwitch] = None 
  var terminationPromise = Promise[Done]()

  def isRunning(): Boolean = {
    source.isDefined
  }

  def logcatSource(): Source[LogOutput, _] = {
    if(source.isDefined) {
      source.get
    } else {
      createLogcatSource()
      source.get
    }
  }
  
  def stopLogcat(): Unit = {
    source match {
      case Some(source) => {
        terminationPromise.success(Done)

        killSwitch.get.shutdown()
      }
      case None => println("Logcat is not running")
    }
    source = None
  }

  def addFilter(filter: String): Unit = {
    source match {
      case Some(source) => {
        source.filter(it => it match {
          case LogMessage(_, _,_, message) => message.contains(filter)
          case _ => true
        })
      }
      case None => println("Logcat is not running")
    }
  }

  private def createLogcatSource(): Unit = { 
    source = Some(Source.actorRef[String](bufferSize = 1000, OverflowStrategy.dropHead)
      .viaMat(KillSwitches.single)(Keep.both)
      .mapMaterializedValue { case (actorRef, killSwitch) =>
        this.killSwitch = Some(killSwitch)
        val processLogger = ProcessLogger(line => {
          //println("Logcat: " + line)
          actorRef ! line
        }) 
        val logcatProcess = "adb logcat -v brief".run(processLogger)
        terminationPromise.future.onComplete(_ => {
         logcatProcess.destroy() // Stop the logcat process when stream is terminated
         terminationPromise = Promise[Done]()
        })(system.dispatcher)
        logcatProcess
      }
      .map(parser.parse))
  }
}

