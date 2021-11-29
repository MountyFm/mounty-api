package routes.room

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import kz.mounty.fm.amqp.messages.MountyMessages.RoomCore
import kz.mounty.fm.domain.requests._
import org.json4s.jackson.Serialization.write
import routes.RouteCompletion

import scala.concurrent.ExecutionContext

trait RoomRoutes extends RouteCompletion {
  val exchange = "X:mounty-api-in"

  def roomRoutes(implicit ex: ExecutionContext,
                 publisher: ActorRef,
                 system: ActorSystem,
                 timeout: Timeout) = pathPrefix("explore") {
    get {
      parameters("size".as[Int]) { size =>
        ctx =>
          val bodyJson = write(GetRoomsForExploreRequestBody(size))
          completeRequest(publisher, bodyJson, RoomCore.GetRoomsForExploreRequest.routingKey, exchange, ctx)
      }
    }
  } ~ pathPrefix("room-and-track") {
    get {
      parameters(
        "roomId".as[String],
        "limit".as[Int].optional,
        "offset".as[Int].optional,
        "tokenKey".as[String]
      ) { (roomId, limit, offset, tokenKey) =>
        ctx =>
          val bodyJson = write(GetRoomAndRoomTracksRequestBody(roomId, limit, offset, tokenKey))

          completeRequest(publisher, bodyJson, RoomCore.GetRoomsAndRoomTracksRequest.routingKey, exchange, ctx)

      }
    }
  } ~ pathPrefix("current-user-rooms") {
    get {
      parameters(
        "limit".as[Int].optional,
        "offset".as[Int].optional,
        "tokenKey".as[String]
      ) { (limit, offset, tokenKey) =>
        ctx =>
          val bodyJson = write(GetCurrentUserRoomsRequestBody(limit, offset, tokenKey))
          completeRequest(publisher, bodyJson, RoomCore.GetCurrentUserRoomsRequest.routingKey, exchange, ctx)
      }
    }
  } ~ pathEndOrSingleSlash {
    get {
      parameters(
        "inviteCode".as[String]
      ) { inviteCode =>
        ctx =>
          val bodyJson = write(GetRoomByInviteCodeRequestBody(inviteCode))
          completeRequest(publisher, bodyJson, RoomCore.GetRoomByInviteCodeRequest.routingKey, exchange, ctx)
      }
    }
  }

}
