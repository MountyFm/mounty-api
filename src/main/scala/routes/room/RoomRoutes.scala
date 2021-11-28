package routes.room

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import kz.mounty.fm.amqp.messages.MountyMessages.RoomCore
import kz.mounty.fm.amqp.messages.MountyMessages.RoomCore.GetRoomsForExploreRequest
import kz.mounty.fm.domain.requests.{GetRoomAndRoomTracksRequestBody, GetRoomsForExploreRequestBody}
import org.json4s.jackson.Serialization.write
import routes.RouteCompletion

import scala.concurrent.ExecutionContext

trait RoomRoutes extends RouteCompletion{
  val exchange = "X:mounty-api-in"
  def roomRoutes(implicit ex: ExecutionContext,
                 publisher: ActorRef,
                 system: ActorSystem,
                 timeout: Timeout) = pathPrefix("explore") {
    get {
      parameters("size".as[Int]) { size => ctx =>
        val body = write(GetRoomsForExploreRequestBody(size))
        completeRequest(publisher, write(body), RoomCore.GetRoomsForExploreRequest.routingKey, exchange,  ctx)
      }
    }
  } ~ pathPrefix("room-and-track") {
    get {
      parameters(
        "roomId".as[String],
        "limit".as[Int].optional,
        "offset".as[Int].optional,
        "tokenKey".as[String]
      ) { (roomId, limit, offset, tokenKey) => ctx =>
        val body = write(GetRoomAndRoomTracksRequestBody(roomId, limit, offset, tokenKey))

        completeRequest(publisher, write(body), RoomCore.GetRoomsAndRoomTracksRequest.routingKey, exchange,  ctx)

      }
    }
  }

}
