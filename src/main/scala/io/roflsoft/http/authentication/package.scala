package io.roflsoft.http

import java.util.UUID

import akka.http.scaladsl.server.directives.{AuthenticationDirective, Credentials}
import akka.http.scaladsl.server.Directives.authenticateOAuth2
import io.roflsoft.db.session.ROSessionStore

package object authentication {

  def authenticate(implicit sessionStore: ROSessionStore): AuthenticationDirective[Session] =
    authenticateOAuth2("site",
    { case p @ Credentials.Provided(token) => sessionStore.get(token)
      case _ => None }
  )

  def authenticateWithRole(roleNames: String*)(implicit sessionStore: ROSessionStore): AuthenticationDirective[Session] = {
    authenticateOAuth2("site",
      { case p @ Credentials.Provided(token) => sessionStore.getIfHasRoles(token, roleNames: _*)
      case _ => None }
    )
  }

  def authenticateWithPermission(permissionNames: String*)(implicit sessionStore: ROSessionStore): AuthenticationDirective[Session] = {
    authenticateOAuth2("site",
      { case p @ Credentials.Provided(token) => sessionStore.getIfHasPermissions(token, permissionNames: _*)
      case _ => None }
    )
  }
}
