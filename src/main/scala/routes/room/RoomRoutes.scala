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
  def roomRoutes(implicit ex: ExecutionContext,
                 publisher: ActorRef,
                 system: ActorSystem,
                 timeout: Timeout) = pathPrefix("explore") {
    get {
      parameters("size".as[Int]) { size =>
        ctx =>
          val bodyJson = write(GetRoomsForExploreRequestBody(size))
          completeRequest(publisher, bodyJson, RoomCore.GetRoomsForExploreRequest.routingKey, ctx)
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

          completeRequest(publisher, bodyJson, RoomCore.GetRoomsAndRoomTracksRequest.routingKey, ctx)

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
          completeRequest(publisher, bodyJson, RoomCore.GetCurrentUserRoomsRequest.routingKey, ctx)
      }
    }
  } ~ pathPrefix("update") {
    put {
      entity(as[UpdateRoomRequestBody]) { entity =>
        ctx =>
          completeRequest(publisher, write(entity), RoomCore.UpdateRoomRequest.routingKey, ctx)
      }
    }
  } ~ pathEndOrSingleSlash {
    get {
      parameters(
        "inviteCode".as[String]
      ) { inviteCode =>
        ctx =>
          val bodyJson = write(GetRoomByInviteCodeRequestBody(inviteCode))
          completeRequest(publisher, bodyJson, RoomCore.GetRoomByInviteCodeRequest.routingKey, ctx)
      }
    }
  }

}
