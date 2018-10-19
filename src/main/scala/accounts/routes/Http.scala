package accounts.routes

import cats.effect._

import accounts.model._
import accounts.service._

import org.http4s.{Service => _, _}
import org.http4s.dsl.io._


import scala.language.higherKinds

case class Http[F[_] : ContextShift](
  service: Service
)(
  implicit F: Effect[F],
  listDecoder: EntityDecoder[IO, List[Record]],
  singleEncoder: EntityEncoder[IO, Record]
) {

  /**
    * create http routes, binds application service to RESTful endpoints
    * @return http routes
    */
  def routes: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case req@POST -> Root / "transactions" =>
        for {
          records <- req.as[List[Record]]
          result <- service.append(records)
          response <- if (result)
            Accepted("transactions processed")
          else
            BadRequest("unable to process account deltas")
        } yield {
          response
        }

      case GET -> Root / "balance" / LongVar(id) =>
        for {
          result <- service.get(id)
          response <- result match {
            case Some(entity) => Ok(entity)
            case None => NotFound(s"entity with $id not found")
          }
        } yield {
          response
        }

      case GET -> Root / "build-info" =>
        Ok(accounts.accountsapi.BuildInfo.toString)
    }
  }

}
