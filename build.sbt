import org.scalajs.linker.interface.{ModuleInitializer, ModuleSplitStyle, OutputPatterns}

lazy val securityProject = RootProject(uri("file:///c:/git/lokallykke-security"))

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := """lokallykke-site""",
    //ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-java8-compat" % VersionScheme.Always,
    //evictionRules += "org.scala-lang.modules" %% "scala-java8-compat" % VersionScheme.Always,
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      ws,
      guice,
      "org.webjars" % "bootstrap" % "4.6.0",
      "org.webjars" % "jquery" % "3.5.1",
      "org.webjars" % "requirejs" % "2.3.6",
      "com.typesafe.slick" %% "slick" % "3.3.2",
      "com.zaxxer" % "HikariCP" % "2.4.2",
      "com.h2database" % "h2" % "1.4.200",
      "org.postgresql" % "postgresql" % "42.2.24",
      "org.jsoup" % "jsoup" % "1.13.1",
      "com.google.api-client" % "google-api-client" % "1.32.1",
      "com.auth0" % "java-jwt" % "3.18.2"
    ),
    resourceGenerators in Compile += Def.task {
      val clientTarget = ( fastLinkJS in Compile in client).value.data
      val bundles = (webpack in fastOptJS in Compile in client).value.map(_.data)
      bundles.foreach(bund => println(s"Generated bundle: ${bund.getAbsolutePath} during webpack"))
      bundles
    }.taskValue,
    PlayKeys.devSettings ++= IO.readLines(new File("./conf/devrunsettings.conf")).map(_.split("=")).map(p => p(0) -> p(1))
  )
  .enablePlugins(PlayScala, LauncherJarPlugin)
  .dependsOn(client)
  .dependsOn(securityProject)
  .aggregate(securityProject)


val circeVersion = "0.13.0"

lazy val client = (project in file("client"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.2.0",
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %% "circe-generic-extras" % circeVersion,
      "org.querki" %%% "jquery-facade" % "2.0" excludeAll(ExclusionRule(organization = "org.scala-js")),
      "io.github.cquiroz" %%% "locales-minimal-en-db" % "1.1.1",
      "io.github.cquiroz" %%% "scala-java-time" % "2.2.0"
    ),
    npmDependencies in Compile ++= Seq(
      "webpack-merge" -> "5.7.3",
      "style-loader" -> "2.0.0",
      //"@popperjs/core"-> "2.0.0",
      "popper.js" -> "1.16.1",
      "jquery" -> "3.6.0",
      "bootstrap" -> "4.6.0",
      "@types/selectize" -> "0.12.34",
      "selectize" -> "0.12.06",
      "@editorjs/editorjs" -> "2.19.3",
      "@editorjs/header" -> "2.6.1",
      "@editorjs/image" -> "2.6.0",
      "@editorjs/list" -> "1.6.2",
      "@editorjs/embed" -> "2.4.0"
    ),
    stIgnore := List("sass-loader", "jquery", "bootstrap","webpack-merge","style-loader","@editorjs/header","@editorjs/image","@editorjs/list","@editorjs/embed"),
    sourceGenerators in Compile += Def.task {
      val _ = (npmInstallDependencies in Compile).value
      Seq.empty[File]
    },
    webpackBundlingMode := BundlingMode.LibraryAndApplication(),
    webpackConfigFile := Some(baseDirectory.value / "../conf/lokallykke.webpack.config.js"),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule)}
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb, ScalaJSBundlerPlugin, ScalablyTypedConverterPlugin)


lazy val commonSettings = Seq(
  scalaVersion := "2.13.5",
  organization := "lokallykke.dk",
  version := "1.0-SNAPSHOT",
  libraryDependencies ++= Seq(
    "commons-io" % "commons-io" % "2.8.0"
  )
)


