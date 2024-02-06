package com.swantosaurus.catlog.input

import akka.Done
import akka.actor.ActorSystem
import akka.stream.*
import akka.stream.scaladsl.*
import com.swantosaurus.catlog.input.LogParser
import com.swantosaurus.catlog.model.{LogMessage, LogOutput}
import org.jline.reader.*
import org.jline.terminal.*

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.sys.process.*

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

