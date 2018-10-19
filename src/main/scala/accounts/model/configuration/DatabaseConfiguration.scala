package accounts.model.configuration

case class DatabaseConfiguration(
  driver:String,
  url: String,
  password: String,
  user: String
)
