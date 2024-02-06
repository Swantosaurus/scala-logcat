package com.swantosaurus.catlog.filter

import com.swantosaurus.catlog.model.LogMessage

trait LogFilter {
  def filter(log: Option[LogMessage]): Option[LogMessage]
}
