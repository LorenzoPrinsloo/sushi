package io.roflsoft.http.server

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

trait WebServer extends App {

  implicit val system: ActorSystem
  implicit val mat: Materializer
  implicit val ec: ExecutionContext

  val routes: Route
  val interface: String
  val web_port: Int
  val logger: Logger

  def start(routes: Route, interface: String, port: Int = web_port): Unit

  override def main(args: Array[String]): Unit = {
    super.main(args)
    start(routes, interface)
    Await.result(system.whenTerminated, Duration.Inf)
  }
}
