name := "sushi"
organization := "io.roflsoft"
version := "0.0.2"
scalaVersion := "2.12.7"
scalacOptions += "-Ypartial-unification"

lazy val akkaHttpVersion = "10.1.9"
lazy val akkaVersion     = "2.6.0-M5"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.0.0-RC1",
  "org.tpolecat" %% "doobie-core"      % "0.7.0",
  "io.circe" %% "circe-core" % "0.12.0-M4",
  "io.circe" %% "circe-generic"  % "0.12.0-M4",
  "io.circe" %% "circe-parser"  % "0.12.0-M4",
  "io.monix" %% "monix" % "3.0.0-RC4",
  "joda-time" % "joda-time" % "2.10.3",
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "net.debasishg" %% "redisclient" % "3.10",
  "com.beachape" % "enumeratum_2.12" % "1.5.13",
  "com.beachape" % "enumeratum-doobie_2.12" % "1.5.15",
  "com.beachape" % "enumeratum-circe_2.12" % "1.5.15"
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
