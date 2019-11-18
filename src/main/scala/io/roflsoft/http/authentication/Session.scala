package io.roflsoft.http.authentication

import org.joda.time.LocalDateTime

import scala.util.control.NonFatal

case class Session(oAuthCredentials: AuthCredentials, token: AuthToken, createdAt: LocalDateTime = LocalDateTime.now())
object Session {
  def apply(values: Map[String, String]): Option[Session] = try {
    Some(
      new Session(
        AuthCredentials(values("username"), values("password")),
        AuthToken(values("access_token"), values("token_type"), values("expires_in").toInt),
        LocalDateTime.parse(values("createdAt"))
      )
    )
  } catch {
    case NonFatal(ex) => None
  }

  def unapply(arg: Session): Map[String, String] = try {
    Map(
      "username" -> arg.oAuthCredentials.username,
      "password" -> arg.oAuthCredentials.password,
      "access_token" -> arg.token.access_token,
      "token_type" -> arg.token.token_type,
      "expires_in" -> arg.token.expires_in.toString,
      "createdAt" -> arg.createdAt.toString("yyyy-MM-dd HH:mm:ss")
    )
  } catch {
    case NonFatal(ex) => Map.empty[String, String]
  }
}
