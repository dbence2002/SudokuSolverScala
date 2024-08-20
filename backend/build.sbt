ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .settings(
    name := "SudokuHttp"
  )

libraryDependencies += "dev.zio" %% "zio-http" % "3.0.0-RC9"
libraryDependencies += "dev.zio" %% "zio-json" % "0.7.2"