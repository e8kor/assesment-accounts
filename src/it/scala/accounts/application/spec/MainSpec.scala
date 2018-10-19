package accounts.application.spec

import accounts.application.Main
import accounts.application.model.Arguments
import accounts.model._
import accounts.model.configuration._
import cats.effect._
import cats.effect.internals.IOContextShift
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.client.blaze._
import org.http4s._
import org.scalatest._

class MainSpec extends WordSpec with Matchers with BeforeAndAfterAll {

  implicit private val cs: ContextShift[IO] = IOContextShift.global
  implicit private val recordsDecoder: EntityDecoder[IO, List[Record]] = jsonOf[IO, List[Record]]
  implicit private val recordsEncoder: EntityEncoder[IO, List[Record]] = jsonEncoderOf[IO, List[Record]]
  implicit private val recordDecoder: EntityDecoder[IO, Record] = jsonOf[IO, Record]
  implicit private val recordEncoder: EntityEncoder[IO, Record] = jsonEncoderOf[IO, Record]

  private val config = Configuration.load(Arguments.emtpy).unsafeRunSync()
  private val url = s"http://${config.server.host}:${config.server.port}"
  private val client = Http1Client[IO]().unsafeRunSync()
  private val server = Main.prepare(Arguments.emtpy).unsafeRunSync().start.unsafeRunSync()

  override def afterAll(): Unit = {
    server.shutdown.unsafeRunSync()
    client.shutdown.unsafeRunSync()
  }

  "http server" should {

    "create transaction record" in {
      val record = Record(1, 100)
      val request = Request[IO](
        method = Method.POST,
        uri = Uri.unsafeFromString(s"$url/transactions")
      ).withEntity(List(record).asJson)
      val status = client.status(request).unsafeRunSync()

      assert(status === Status.Accepted)
    }

    "create more transaction records" in {
      val record1 = Record(1, 100)
      val record2 = Record(2, 100)
      val record3 = Record(1, -50)
      val request = Request[IO](
        method = Method.POST,
        uri = Uri.unsafeFromString(s"$url/transactions")
      ).withEntity(List(record1, record2, record3).asJson)
      val status = client.status(request).unsafeRunSync()

      assert(status === Status.Accepted)
    }

    "return account 1 balance" in {
      val record = Record(1, 150)
      val response = client.expect[Record](Uri.unsafeFromString(s"$url/balance/${record.id}")).unsafeRunSync()

      assert(response === record)
    }

    "return account 2 balance" in {
      val record = Record(2, 100)
      val response = client.expect[Record](Uri.unsafeFromString(s"$url/balance/${record.id}")).unsafeRunSync()

      assert(response === record)
    }

  }

}