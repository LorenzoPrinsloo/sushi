package io.roflsoft.db

trait PostgresModel extends Product with Serializable {
  def id: Long
}
