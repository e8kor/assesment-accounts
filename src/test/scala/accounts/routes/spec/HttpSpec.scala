package accounts.routes.spec

import accounts.model.Record
import org.scalatest._
import io.circe._
import io.circe.syntax._
import cats.effect._
import cats.effect.internals.IOContextShift
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

class HttpSpec extends WordSpec with Matchers {

  implicit private val cs: ContextShift[IO] = IOContextShift.global
  implicit private val recordsDecoder: EntityDecoder[IO, List[Record]] = jsonOf[IO, List[Record]]
  implicit private val recordsEncoder: EntityEncoder[IO, List[Record]] = jsonEncoderOf[IO, List[Record]]
  implicit private val recordDecoder: EntityDecoder[IO, Record] = jsonOf[IO, Record]
  implicit private val recordEncoder: EntityEncoder[IO, Record] = jsonEncoderOf[IO, Record]

  "AccountService" should {

    "create a transaction record" in {
      val id = 1
      val record = Record(id, 100)
      val service = new ServiceMock(Some(record))
      val routes: HttpRoutes[IO] = accounts.routes.Http(service).routes
      val response = serve(routes, Request[IO](POST, uri("/transactions")).withEntity(List(record)))
      assert(response.status === Status.Accepted)
    }

    "return account balance" in {
      val record = Record(1, 100)
      val service = new ServiceMock(Some(record))
      val routes: HttpRoutes[IO] = accounts.routes.Http(service).routes
      val response = serve(routes, Request[IO](GET, Uri.unsafeFromString(s"/balance/${record.id}")))
      assert(response.status === Status.Ok)
      assert(response.as[Record].unsafeRunSync() === record)
    }

  }

  private def serve(routes: HttpRoutes[IO], request: Request[IO]): Response[IO] = {
    routes.orNotFound.run(request).unsafeRunSync()
  }

}
