ThisBuild / scalaVersion := "3.3.0"
name := "scala-logcat"

run := (catlog / Compile / run).evaluated 

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



