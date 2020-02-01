name := "sushi"
organization := "io.roflsoft"
version := "0.0.2"
scalaVersion := "2.13.1"

lazy val akkaHttpVersion = "10.1.11"
lazy val akkaVersion     = "2.5.26"
lazy val circeVersion    = "0.12.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.tpolecat" %% "doobie-core" % "0.8.7",
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
  "com.beachape" % "enumeratum-circe_2.13" % "1.5.21",
  "com.lightbend.akka" %% "akka-stream-alpakka-sqs" % "1.1.2"
)

lazy val codegen =
  (project in file("codegen"))
  .settings(
    name := "sushi-codegen",
    organization := "io.roflsoft",
    version := "0.1-SNAPSHOT",
    sbtPlugin := true,
    resolvers ++= Seq(
      "zalando-maven" at "https://dl.bintray.com/zalando/maven"
    ),
    libraryDependencies += "de.zalando" %% "beard" % "0.2.0"
  )


// Artifact Deployment

// POM settings for Sonatype
organization := "io.roflsoft"
homepage := Some(url("https://github.com/LorenzoPrinsloo/sushi"))
scmInfo := Some(ScmInfo(url("https://github.com/LorenzoPrinsloo/sushi"), "git@github.com:LorenzoPrinsloo/sushi.git"))
developers := List(Developer("LorenzoPrinsloo",
  "Lorenzo Prinsloo",
  "cmlprinsloo@gmail.com",
  url("https://github.com/LorenzoPrinsloo")))
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
publishMavenStyle := true
pgpReadOnly := false

// Add sonatype repository settings
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
