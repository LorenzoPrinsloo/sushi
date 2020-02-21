package io.roflsoft.db.session

import io.roflsoft.http.authentication.Session

trait ROSessionStore {

  def get(token: String): Option[Session]

  def getIfHasRoles(token: String, roleNames: String*): Option[Session]

  def getIfHasPermissions(token: String, permissionNames: String*): Option[Session]
}
