package com.swantosaurus.catlog

class ProgramStateHolder {
  private var programState = ProgramState.Running

  def get(): ProgramState = synchronized {
    programState
  }

  def setState(to: ProgramState): Unit = synchronized {
    programState = to
  }
}
