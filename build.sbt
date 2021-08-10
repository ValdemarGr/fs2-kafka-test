ThisBuild / scalaVersion := "2.13.6"
ThisBuild / organization := "com.example"

lazy val root = (project in file("."))
  .settings(
    name := "Hello",
    libraryDependencies += "com.github.fd4s" %% "fs2-kafka" % "3.0.0-M3",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.2.2",
  )
