package com.swantosaurus.catlog

import fansi._

case class OutputCell(
  val name: String,
  val width: Int,
  val position: Int,
  val visibility: Boolean,
  val alignment: TextAlignment,
  val textColor: Attr,
  val backgroundColor: Attr,
) { 
  def render(value: String): String = {
    val paddedValue = alignment match {
      case TextAlignment.Left => value.padTo(width, ' ')
      case TextAlignment.Right => value.padLeft(width, ' ')
      case TextAlignment.Center => value.padLeft(width / 2, ' ').padTo(width, ' ')
    }

    (textColor ++ backgroundColor)(paddedValue).render
  }

  extension(s: String) {
    def padLeft(length: Int, char: Char): String = {
      if(s.length >= length) {
        s
      } else {
        char.toString * (length - s.length) + s
      }
    }
  }
}
