package accounts.service

import cats.effect._
import accounts.model._
import accounts.repository._

/**
  * Abstraction layer for dependencies binding
  */
trait Service {

  /**
    * creates transaction records
    * @param items records
    * @return
    */
  def append(items: List[Record]): IO[Boolean]

  /**
    * reads account balance by its id
    * @param id account id
    * @return
    */
  def get(id: Long): IO[Option[Record]]

}

object Service {

  def apply(
    accounts: AccountsRepository
  ): Service = new AccountsService(accounts)

}

/**
  * Service implementation that binds accounts repository
  * @param accounts repository
  */
class AccountsService(
  accounts: AccountsRepository
) extends Service {

  override def append(items: List[Record]): IO[Boolean] = {
    accounts.append(items)
  }

  override def get(id: Long): IO[Option[Record]] = {
    accounts.get(id)
  }

}