package accounts.application

import accounts.routes._
import org.http4s.server._
import org.http4s.server.blaze._
import org.http4s.implicits._
import cats.effect._
import cats.implicits._
import accounts.application.model.Arguments
import accounts.infrastructure._
import accounts.model._
import accounts.model.configuration._
import accounts.repository._
import accounts.service._
import com.typesafe.scalalogging.LazyLogging
import org.http4s.{Http => _, Service => _, _}
import org.http4s.circe._
import scopt.OptionParser

/**
  * Main is application entry point it creates dependencies and http routes
  */
object Main extends IOApp with LazyLogging {

  private lazy val parser: OptionParser[Arguments] = new OptionParser[Arguments](
    "accounts-api"
  ) {
    head("accounts api", "0.0.1-SNAPSHOT")

    help("help").text(
      """
        |'config' must be specified with paths to actual hocon-style config file.
        |""".stripMargin
    )

    opt[String]('c', "config")
      .valueName("config")
      .action((x, c) => c.copy(config = Some(x)))
      .text("'config' defines application configuration to use")

  }

  override def run(rawArgs: List[String]): IO[ExitCode] = {
    parser.parse(rawArgs, Arguments.emtpy) match {
      case Some(args) =>
        logger.info(s"starting application with arguments: $args")
        for {
          builder <- prepare(args)
          status <- builder.serve.compile.drain.as(ExitCode.Success)
        } yield {
          status
        }
      case None =>
        val exception = new IllegalArgumentException(
          s"Failed to parse command-line arguments: ${rawArgs.mkString(", ")}"
        )
        IO.raiseError[ExitCode](exception)
    }
  }

  /**
    * Prepare initialize blaze server builder with http routes and underlying services
    * Method is exposed for integration tests
    * @param args safely parsed application arguments
    * @return io of blaze server builder
    */
  def prepare(args: Arguments): IO[BlazeServerBuilder[IO]] = {

    implicit val recordsDecoder: EntityDecoder[IO, List[Record]] = jsonOf[IO, List[Record]]
    implicit val recordDecoder: EntityEncoder[IO, Record] = jsonEncoderOf[IO, Record]

    for {
      config <- Configuration.load(args)
      server = config.server
      accounts <- config.database match {
        case Some(db) =>
          for {
            transactor <- createTransactor(db)
            _ <- initialize(transactor)
          } yield {
            AccountsRepository(transactor)
          }

        case None =>
          IO(AccountsRepository())

      }
      service = Service(accounts)
      http = Http(service)
      router = Router("/" -> http.routes).orNotFound
      builder = BlazeServerBuilder[IO].bindHttp(server.port, server.host).withHttpApp(router)
    } yield {
      builder
    }
  }

}