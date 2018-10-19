package accounts.model

/**
  * Application abstraction for keeping information about account balance evolution.
  * Delta of: 50 is 50 cents, 150 is 1 dollar 50 cents
  * Double or float are not good when it comes to deal with money
  * @param id account id
  * @param delta balance delta
  */
case class Record(id: Long, delta: Long)

object Record {

  import io.circe._, io.circe.generic.semiauto._

  implicit val Decoder: Decoder[Record] = deriveDecoder[Record]
  implicit val Encoder: ObjectEncoder[Record] = deriveEncoder[Record]

}