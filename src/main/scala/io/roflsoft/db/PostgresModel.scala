package io.roflsoft.db

import java.util.UUID

trait PostgresModel extends Product with Serializable {
  def uuid: UUID
}
