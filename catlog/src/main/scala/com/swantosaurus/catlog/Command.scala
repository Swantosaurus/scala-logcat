package com.swantosaurus.catlog

case class Command(command: String, description: String, action: () => Unit)

