package accounts.repository

import cats.effect.IO
import accounts.model._
import doobie.util.update._
import doobie.implicits._
import cats.implicits._
import doobie.util.transactor.Transactor

trait AccountsRepository {

  def append(items: List[Record]): IO[Boolean]

  def get(id: Long): IO[Option[Record]]

}

object AccountsRepository {

  def apply(): AccountsRepository = new InMemoryAccountsRepository()

  def apply(
    transactor: Transactor[IO]
  ): AccountsRepository = {
    new H2AccountsRepository(transactor)
  }

}

class InMemoryAccountsRepository() extends AccountsRepository {

  import scala.collection.mutable

  private val store: mutable.LongMap[Long] = new mutable.LongMap[Long](_ => 0)

  override def append(items: List[Record]): IO[Boolean] = {
    IO {
      for (Record(id, delta) <- items) {
        store.update(id, store(id) + delta)
      }

      true
    }
  }

  override def get(id: Long): IO[Option[Record]] = {
    IO(store.get(id).map(Record(id, _)))
  }

}

class H2AccountsRepository(
  transactor: Transactor[IO]
) extends AccountsRepository {

  import scala.language.postfixOps

  override def append(items: List[Record]): IO[Boolean] = {
    if (items.isEmpty) {
      IO(false)
    } else {
      val sql = "insert into accounts_history (id, delta) values (?, ?)"
      Update[Record](sql).updateMany(items).transact(transactor).map(items.length ==)
    }
  }

  override def get(id: Long): IO[Option[Record]] = {
    sql"select * from accounts where id = $id".query[Record].option.transact(transactor)
  }

}
