package io.roflsoft.http.authentication

case class AuthToken(access_token: String = java.util.UUID.randomUUID().toString,
                     token_type: String = "bearer",
                     expires_in: Int = 3600)
