package io.roflsoft.http.authentication

import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.language.postfixOps

case class AuthToken(access_token: String = java.util.UUID.randomUUID().toString,
                     token_type: String = "bearer",
                     expires_in: Duration = 5 minutes)
