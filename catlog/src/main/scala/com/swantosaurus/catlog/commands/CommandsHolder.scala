package com.swantosaurus.catlog.commands

import com.swantosaurus.catlog.commands.Command

case class CommandHolder(private val commands: List[Command], private val defaultCommand: Command) {
  def getCommand(command: String): Option[Command] = {
    commands.find(_.command == command) 
  }

  def run(command: String): Unit = {
    getCommand(command).map(_.action()).getOrElse(defaultCommand.action())
  }

  def getCommandDescription(command: String): Option[String] = {
    getCommand(command).map(_.description)
  }

  def getCommandList(): List[String] = {
    commands.map(_.command)
  }

  def getCommandDescriptionList(): List[String] = {
    commands.map(_.description)
  }

  def getHelpString() : String = {
    val commandList = getCommandList()
    val commandDescriptionList = getCommandDescriptionList()
    val commandListWithDescription = commandList.zip(commandDescriptionList)
    val commandListWithDescriptionString = commandListWithDescription.map { case (command, description) => s"$command - $description" }
    commandListWithDescriptionString.mkString("\n")
  }
}

