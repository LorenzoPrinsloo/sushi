package io.roflsoft.http

import akka.http.scaladsl.server.directives.{AuthenticationDirective, Credentials}
import akka.http.scaladsl.server.Directives.authenticateOAuth2
import io.roflsoft.db.session.SessionStore

package object authentication {

  def authenticate(sessionStore: SessionStore): AuthenticationDirective[Session] =
    authenticateOAuth2("site",
    { case p@Credentials.Provided(token) => sessionStore.get(token, sessionStore.fields(): _*).flatMap(m => Session(m))
      case _ => None }
  )
}
