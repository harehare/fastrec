lazy val scalaJSReactVersion = "1.7.7"
lazy val diodeVersion        = "1.1.14"

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.fastrec"
ThisBuild / organizationName := "harehare"
ThisBuild / developers := List(
  Developer(
    id = "harehare",
    name = "Takahiro Sato",
    email = "harehare1110@gmail.com",
    url = url("https://harehare.github.io/flutter-portfolio")
  )
)
ThisBuild / scalafixDependencies += "com.nequissimus"      %% "sort-imports"     % "0.5.0"
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
ThisBuild / scalafixDependencies += "com.github.vovapolu"  %% "scaluzzi"         % "0.1.18"
scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) }

lazy val commonSettings = Seq(
  scalacOptions ++= List(
    "-deprecation",
    "-feature",
    "-Xlint",
    "-unchecked",
    "-Yrangepos",
    "-Xfatal-warnings",
    "-Yrangepos",
    "-Wunused:imports",
    "-Ymacro-annotations"
  ),
  addCompilerPlugin(scalafixSemanticdb),
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision
)

lazy val root = project
  .in(file("."))
  .aggregate(client)

lazy val client = project
  .in(file("client"))
  .settings(commonSettings)
  .settings(
    name := "FastRec",
    scalaJSUseMainModuleInitializer := true,
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      "org.scala-js"                            %%% "scalajs-dom"          % "1.1.0",
      "com.github.japgolly.scalajs-react"       %%% "core"                 % scalaJSReactVersion,
      "com.github.japgolly.scalajs-react"       %%% "extra"                % scalaJSReactVersion,
      "org.typelevel"                           %%% "mouse"                % "0.26.2",
      "io.suzaku"                               %%% "diode"                % diodeVersion,
      "io.suzaku"                               %%% "diode-react"          % diodeVersion,
      "com.softwaremill.macwire"                %% "macros"                % "2.3.7",
      "com.github.fdietze.scala-js-fontawesome" %%% "scala-js-fontawesome" % "-SNAPSHOT",
      "org.typelevel"                           %%% "cats-core"            % "2.1.1",
      "eu.timepit"                              %%% "refined"              % "0.9.20",
      "io.estatico"                             %%% "newtype"              % "0.4.4"
    ),
    npmDependencies in Compile ++= Seq(
      "react"          -> "17.0.1",
      "react-dom"      -> "17.0.1",
      "recordrtc"      -> "5.6.1",
      "@ffmpeg/ffmpeg" -> "0.9.7",
      "pretty-bytes"   -> "5.5.0",
      "blob-util"      -> "2.0.2"
    ),
    npmDevDependencies in Compile ++= Seq(
      "webpack-merge"           -> "5.7.3",
      "file-loader"             -> "5.1.0",
      "image-webpack-loader"    -> "6.0.0",
      "css-loader"              -> "5.2.0",
      "style-loader"            -> "1.1.3",
      "url-loader"              -> "3.0.0",
      "html-webpack-plugin"     -> "4.5.2",
      "preload-webpack-plugin"  -> "3.0.0-beta.4",
      "mini-css-extract-plugin" -> "0.9.0",
      "sass"                    -> "1.32.8",
      "sass-loader"             -> "7.3.1",
      "workbox-sw"              -> "^5.1.3",
      "workbox-webpack-plugin"  -> "^5.1.3",
      "clean-webpack-plugin"    -> "^3.0.0",
      "copy-webpack-plugin"     -> "^6.4.1"
    ),
    version in webpack := "4.46.0",
    webpackConfigFile in fastOptJS := Some(
      baseDirectory.value / "webpack" / "dev.webpack.config.js"
    ),
    webpackConfigFile in fullOptJS := Some(
      baseDirectory.value / "webpack" / "prod.webpack.config.js"
    )
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)

addCommandAlias("start", "fastOptJS::startWebpackDevServer")
addCommandAlias("dev", ";start; fastOptJS")
addCommandAlias("prod", "fullOptJS::webpack")
