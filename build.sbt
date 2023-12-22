ThisBuild / scalaVersion := "3.3.0"
name := "test"

lazy val global = project
  .in(file("."))
  .settings(
    name := "global",
  )

lazy val strings = project
  .in(file("strings"))
  .settings(
    name := "strings",
  )

lazy val catlog = project
  .in(file("catlog"))
  .settings(
    name := "catlog",
  )
  .dependsOn(strings)

lazy val commandLinePrinter = project
  .in(file("commandLinePrinter"))
  .settings(
    name := "commandLinePrinter",
  )

lazy val main = project
  .in(file("main"))
  .settings(
    name := "main",
  )
  .dependsOn(global, catlog, commandLinePrinter)


