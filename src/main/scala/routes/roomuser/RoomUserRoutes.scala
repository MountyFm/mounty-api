package routes.roomuser

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{entity, pathPrefix, _}
import akka.util.Timeout
import kz.mounty.fm.amqp.messages.MountyMessages.RoomCore
import kz.mounty.fm.domain.requests._
import kz.mounty.fm.domain.user.RoomUserType
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
        "roomId".as[String],
        "type".as[String]?,
      ) { (roomId, `type`) =>
        ctx =>
          val bodyJson = write(GetRoomUsersRequestBody(
            roomId = roomId,
            `type` = if(`type`.isDefined) Some(RoomUserType(`type`.get)) else None))
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