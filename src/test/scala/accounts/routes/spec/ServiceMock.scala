package accounts.routes.spec

import accounts.model.Record
import accounts.service.Service
import cats.effect.IO

class ServiceMock(record: Option[Record] = None) extends Service {

  override def append(items: List[Record]): IO[Boolean] = IO(true)

  override def get(id: Long): IO[Option[Record]] = IO(record)

}
