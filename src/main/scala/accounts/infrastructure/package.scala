package accounts

import accounts.model.configuration.DatabaseConfiguration
import doobie.hikari.HikariTransactor
import org.flywaydb.core._
import cats.effect._

package object infrastructure {

  /**
    * Safely instantiate transactor, abstraction that stacks and invokes lambdas
    * @param config database configuration for transactor
    * @param cs application execution context pools
    * @return io of datasource transactor
    */
  def createTransactor(config: DatabaseConfiguration)(implicit cs: ContextShift[IO]): IO[HikariTransactor[IO]] = {
    HikariTransactor.newHikariTransactor[IO](
      config.driver,
      config.url,
      config.user,
      config.password
    )
  }

  /**
    * Performs migrations of db schemas
    * @param transactor database transactor
    * @return state of migration
    */
  def initialize(transactor: HikariTransactor[IO]): IO[Int] = {
    transactor.configure { datasource =>
      IO {
        val flyWay = new Flyway()
        flyWay.setDataSource(datasource)
        flyWay.migrate()
      }
    }
  }

}
