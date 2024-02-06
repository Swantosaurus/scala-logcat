package com.swantosaurus.catlog.utils

import scala.collection.Map
import scala.sys.process.*

class ProcessReader {
  private var pidToPackageName: Option[Map[Int, String]] = None 
  private var packageNameToPid: Option[Map[String, Int]] = None
  readAdbPs()

  def getProcessPackageName(pid: Int): Option[String] = { 
    pidToPackageName match {
      case Some(map) => map.get(pid)
      case None => None
    }
  }

  def getProcessPidByPartOfName(partOfName: String): Option[Int] = {
    packageNameToPid match {
      case Some(map) => map.find(it => it._1.contains(partOfName)) match {
        case Some((_, pid)) => Some(pid)
        case None => None
      }
      case None => None
    }
  }
  
  def getProcessPid(packageName: String): Option[Int] = {
    packageNameToPid match {
      case Some(map) => map.get(packageName)
      case None => None
    }
  }

  def readAdbPs(): Unit = {
    try { 
      val adbPs = ("adb shell ps" #| "grep \"^u\"" #| Seq("awk", "{print $2\" \"$9}")).!!
      pidToPackageName = Some(adbPs.split("\n").map(it => {
        val split = it.split(" ")
        (split(0).toInt, split(1))
      }).toMap)
      packageNameToPid = Some(pidToPackageName.get.map(_.swap))
    } catch {
      case e: Exception => println("Error reading adb ps: " + e)
    }
  }
} 
