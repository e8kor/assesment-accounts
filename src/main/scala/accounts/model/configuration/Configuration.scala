package accounts.model.configuration

import java.io.File

import accounts.application.model.Arguments
import cats.effect.IO
import com.typesafe.config._
import pureconfig.error._

import scala.util._

case class Configuration(
  server: ServerConfiguration,
  database: Option[DatabaseConfiguration]
)

object Configuration {

  /**
    * with arguments parse and instantiate application configuration
    * @param args application arguments
    * @return io of application configuration
    */
  def load(args: Arguments): IO[Configuration] = {
    import pureconfig._

    val Arguments(config) = args

    val app = config match {

      case Some(value) =>
        val fConfig = new File(value)
        ConfigFactory.parseFile(fConfig).resolve()

      case None =>
        ConfigFactory.load().resolve()

    }

    val configurationIO: IO[Configuration] = loadConfig[Configuration](app) match {
      case Left(e) => IO.raiseError[Configuration](new ConfigReaderException[Config](e))
      case Right(s) => IO.pure(s)
    }

    configurationIO
  }

}