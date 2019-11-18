package io.roflsoft.db.session

import com.redis._
import serialization._
import Parse.Implicits._
import io.roflsoft.http.authentication.{AuthCredentials, AuthToken, Session}
import io.roflsoft.reflection.utils.members


class RedisSessionStore(client: RedisClient) extends SessionStore {
  override def set[A, B](key: String, value: Map[A, B]): Boolean = client.hmset(key, value)

  override def get(key: String, fields: String*): Option[Map[String, String]] = client.hmget[String, String](key, fields: _*)

  override def fields(): Seq[String] = members[AuthCredentials] ++ members[AuthToken] :+ "createdAt"
}
