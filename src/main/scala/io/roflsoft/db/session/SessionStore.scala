package io.roflsoft.db.session

trait SessionStore {

  def set[A, B](key: String, value: Map[A, B]): Boolean
  def get(key: String, fields: String*): Option[Map[String, String]]

  def fields(): Seq[String]
}
