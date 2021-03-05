import org.scalajs.linker.interface.{ModuleInitializer, ModuleSplitStyle, OutputPatterns}



lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := """lokallykke-site""",
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
//    pipelineStages := Seq(digest, gzip)
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      guice,
      "org.webjars" % "bootstrap" % "4.6.0",
      "org.webjars" % "jquery" % "3.5.1",
      "org.webjars" % "requirejs" % "2.3.6"
    ),
    resourceGenerators in Compile += Def.task {
      val clientTarget = ( fastLinkJS in Compile in client).value.data
      val bundles = (webpack in fastOptJS in Compile in client).value.map(_.data)
      bundles.foreach(bund => println(s"Generated bundle: ${bund.getAbsolutePath} during webpack"))
      bundles
      //println(s"JS target file: ${clientTarget.getAbsolutePath}")
      //Seq()
    }.taskValue
  )
  .enablePlugins(PlayScala)
  .dependsOn(sharedJvm)


val circeVersion = "0.13.0"

lazy val client = (project in file("client"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.1.0",
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "org.querki" %%% "jquery-facade" % "2.0"
    ),
    npmDependencies in Compile ++= Seq("jquery" -> "3.6.0", "bootstrap" -> "4.6.0"),
    sourceGenerators in Compile += Def.task {
      val _ = (npmInstallDependencies in Compile).value
      Seq.empty[File]
    },
    webpackBundlingMode := BundlingMode.LibraryAndApplication(),
    /*scalaJSUseMainModuleInitializer in Compile := true,
    mainClass := Some("dk.lokallykke.client.Main"),*/
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule)}/*,
    scalaJSStage in Global := FullOptStage,
    scalaJSModuleInitializers in Compile ++= Seq(
      ModuleInitializer.mainMethod("dk.lokallykke.client.accounting.Accounting", "main").withModuleID("accounting"),
      ModuleInitializer.mainMethod("dk.lokallykke.client.shop.Shop", "main").withModuleID("shop")
    )*/
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb, ScalaJSBundlerPlugin)
  .dependsOn(sharedJs)



lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
  .jsConfigure(_.enablePlugins(ScalaJSWeb))
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js


lazy val commonSettings = Seq(
  scalaVersion := "2.13.5",
  organization := "lokallykke.dk",
  version := "1.0-SNAPSHOT",
  libraryDependencies ++= Seq(
    "commons-io" % "commons-io" % "2.8.0"
  )
)
