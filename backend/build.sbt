ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.3"

val Http4sVersion = "1.0.0-M29"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.2.6"
val MunitCatsEffectVersion = "1.0.6"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "SudokuSolver",
    libraryDependencies ++= Seq(
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
      guice,
      ws
    ),
  )