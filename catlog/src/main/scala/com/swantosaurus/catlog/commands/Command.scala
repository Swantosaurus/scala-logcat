package com.swantosaurus.catlog.commands

case class Command(command: String, description: String, action: () => Unit)

