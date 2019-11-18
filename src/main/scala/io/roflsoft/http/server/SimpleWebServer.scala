package io.roflsoft.http.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait SimpleWebServer extends WebServer {

  implicit val system: ActorSystem = ActorSystem("SimpleWebServer")
  implicit val mat: Materializer = ActorMaterializer.create(system)
  implicit val ec: ExecutionContext = system.dispatcher

  val routes: Route
  override val interface: String = sys.env.getOrElse("WEB_INTERFACE", "localhost")
  override val web_port: Int = sys.env.getOrElse("WEB_PORT", "8080").toInt
  override val logger: Logger = Logger(classOf[SimpleWebServer].getTypeName)

  def start(routes: Route, interface: String, port: Int = web_port): Unit = {
    val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, interface, port)

    serverBinding.onComplete {
      case Success(bound) => logger.info(s"${Console.GREEN}Web Server started at https://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/${Console.RESET}")
      case Failure(e) =>
        logger.error(s"${Console.RED}Server could not start!${Console.RESET}", e)
        system.terminate()
    }
  }
}
