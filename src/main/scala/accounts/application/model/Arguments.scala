package accounts.application.model

/**
  * Parsed application arguments
  * if config is none then load default config from jar, otherwise load external file
  * @param config path to config file
  */
case class Arguments(
  config: Option[String]
)

object Arguments {

  val emtpy = new Arguments(None)

}