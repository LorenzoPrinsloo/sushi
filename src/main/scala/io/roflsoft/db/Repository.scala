package io.roflsoft.db

trait Repository[A, F[_]] extends RORepository[A, F] {

  def add(item: A): F[A]

  def add(items: A*): F[List[A]]

  def update(item: A): F[A]

  def remove(item: A): F[Int]

  def removeByQuery(q: Query): F[Int]
}
