name := "sushi"
organization := "io.roflsoft"
version := "0.0.2"
scalaVersion := "2.13.1"

lazy val akkaHttpVersion = "10.1.11"
lazy val akkaVersion     = "2.5.26"
lazy val circeVersion    = "0.12.0"

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-reactive-streams" % "2.2.1",
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.tpolecat" %% "doobie-core" % "0.8.7",
  "org.tpolecat" %% "doobie-quill" % "0.8.8",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.monix" %% "monix" % "3.1.0",
  "joda-time" % "joda-time" % "2.10.3",
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "net.debasishg" %% "redisclient" % "3.10",
  "com.beachape" % "enumeratum_2.13" % "1.5.14",
  "com.beachape" % "enumeratum-doobie_2.13" % "1.5.16",
  "com.beachape" %% "enumeratum-quill" % "1.5.15",
  "com.beachape" % "enumeratum-circe_2.13" % "1.5.21",
  "com.lightbend.akka" %% "akka-stream-alpakka-sqs" % "1.1.2"
)


// Artifact Deployment

// POM settings for Sonatype
organization in ThisBuild := "io.roflsoft"
homepage in ThisBuild := Some(url("https://github.com/LorenzoPrinsloo/sushi"))
scmInfo in ThisBuild := Some(ScmInfo(url("https://github.com/LorenzoPrinsloo/sushi"), "git@github.com:LorenzoPrinsloo/sushi.git"))
developers in ThisBuild := List(Developer("LorenzoPrinsloo",
  "Lorenzo Prinsloo",
  "cmlprinsloo@gmail.com",
  url("https://github.com/LorenzoPrinsloo")))
licenses in ThisBuild += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
publishMavenStyle in ThisBuild := true
pgpReadOnly in ThisBuild := false

// Add sonatype repository settings
publishTo in ThisBuild := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
