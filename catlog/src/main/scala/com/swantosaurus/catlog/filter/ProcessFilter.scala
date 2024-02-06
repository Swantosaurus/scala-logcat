package com.swantosaurus.catlog.filter

import com.swantosaurus.catlog.filter.LogFilter
import com.swantosaurus.catlog.model.LogMessage
import com.swantosaurus.catlog.utils.ProcessReader

class ProcessFilter(private val packageReader: ProcessReader) extends LogFilter {
  var pid : Option[Int] = None

  def setPid(pid: Option[Int]) : Unit = {
    this.pid = pid
  }
  def filter(log: Option[LogMessage]): Option[LogMessage] = {
    log match {
      case Some(log) => {
        val processName = packageReader.getProcessPackageName(log.processId)
        if(processName.isDefined){
          if(pid.isEmpty || pid.isDefined && pid.get == log.processId) {
            Some(log)
          } else {
            None
          }
        } else {
          None
        }
      }
      case None => None
    }
  }
}
