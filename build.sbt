ThisBuild / scalaVersion := "3.3.0"
name := "scala-logcat"


run := (catlog / Compile / run).evaluated 


lazy val catlog = project
  .in(file("catlog"))
  .settings(
    name := "catlog",
    libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-stream" % "2.8.5",
        "org.jline" % "jline-reader" % "3.19.0",
    )
  )
  .dependsOn(commandLinePrinter)

lazy val commandLinePrinter = project
  .in(file("commandLinePrinter"))
  .settings(
    name := "commandLinePrinter",
    libraryDependencies += "com.lihaoyi" %% "fansi" % "0.4.0"
  )



