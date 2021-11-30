package routes.roomuser

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{entity, pathPrefix, _}
import akka.util.Timeout
import kz.mounty.fm.amqp.messages.MountyMessages.RoomCore
import kz.mounty.fm.domain.requests._
import org.json4s.jackson.Serialization.write
import routes.RouteCompletion

import scala.concurrent.ExecutionContext

trait RoomUserRoutes extends RouteCompletion {
  def roomUserRoutes(implicit ex: ExecutionContext,
                     publisher: ActorRef,
                     system: ActorSystem,
                     timeout: Timeout) = pathEndOrSingleSlash {
    get {
      parameters(
        "roomId".as[String]
      ) { roomId =>
        ctx =>
          val bodyJson = write(GetRoomUsersByRoomIdRequestBody(roomId))
          completeRequest(publisher, bodyJson, RoomCore.GetRoomUsersRequest.routingKey, ctx)
      }
    }
  } ~ pathPrefix("id" / Segment) { id =>
    get {
      ctx =>
        val bodyJson = write(GetRoomUserByIdRequestBody(id))
        completeRequest(publisher, bodyJson, RoomCore.GetRoomUserByIdRequest.routingKey, ctx)
    }
  } ~ pathPrefix("update") {
    put {
      entity(as[UpdateRoomUserRequestBody]) { entity =>
        ctx =>
          completeRequest(publisher, write(entity), RoomCore.UpdateRoomUserRequest.routingKey, ctx)
      }
    }
  } ~ pathPrefix("new") {
    post {
      entity(as[CreateRoomUserIfNotExistRequestBody]) { entity =>
        ctx =>
          completeRequest(publisher, write(entity), RoomCore.CreateRoomUserIfNotExistRequest.routingKey, ctx)
      }
    }
  }
}