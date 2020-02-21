package io.roflsoft.db

trait RORepository[A, F[_]] {

  type Query

  def query(q: Query): F[List[A]]
}
