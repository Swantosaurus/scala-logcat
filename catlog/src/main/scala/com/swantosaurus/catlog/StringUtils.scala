package com.swantosaurus.catlog


extension(s : String) {
  def padLeft(length: Int, char: Char): String = {
    if(s.length >= length) {
      s
    } else {
      char.toString * (length - s.length) + s
    }
  }
}
