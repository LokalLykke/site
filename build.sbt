import play.twirl.sbt.Import.TwirlKeys._
import play.twirl.sbt.TemplateCompiler.collectTemplates

name := """lokallykke-site"""
organization := "lokallykke.dk"

version := "1.0-SNAPSHOT"

lazy val clientProject = RootProject(uri("file:///c:/git/lokallykke-client"))


lazy val root = (project in file("."))
    .enablePlugins(PlayScala)
    .dependsOn(clientProject)
    .aggregate(clientProject)


resourceGenerators in Compile += Def.task {
  val clientTarget = (fastOptJS in Compile in clientProject).value.data
  println(s"JS target file: ${clientTarget.getAbsolutePath}")
  Seq(clientTarget)
}.taskValue

watchSources ++= (watchSources in clientProject).value

scalaVersion := "2.13.5"

libraryDependencies += guice
libraryDependencies += "org.webjars" % "bootstrap" % "4.6.0"
libraryDependencies += "org.webjars" % "jquery" % "3.5.1"
libraryDependencies += "commons-io" % "commons-io" % "2.8.0"

