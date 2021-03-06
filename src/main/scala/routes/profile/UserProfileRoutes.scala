package routes.profile

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import kz.mounty.fm.amqp.messages.MountyMessages.UserProfileCore
import kz.mounty.fm.amqp.messages.PingMessage
import kz.mounty.fm.domain.requests.{DeleteUserProfileRequestBody, GetUserProfileByIdRequestBody, UpdateUserProfileRequestBody}
import org.json4s.jackson.Serialization._
import routes.RouteCompletion
import routes.profile.dto.CreateUserProfileDTO

import scala.concurrent.ExecutionContext

trait UserProfileRoutes extends RouteCompletion {
  def userProfileRoutes(implicit ex: ExecutionContext,
                        publisher: ActorRef,
                        system: ActorSystem,
                        timeout: Timeout) = pathPrefix("ping") {
    post {
      entity(as[PingMessage]) { body =>
        ctx =>
          completeRequest(publisher, write(body), UserProfileCore.Ping.routingKey, ctx)
      }
    }
  } ~ pathPrefix("new") {
    post {
      entity(as[CreateUserProfileDTO]) { body =>
        ctx =>
          val createRequest = CreateUserProfileDTO.convert(body)
          completeRequest(publisher, write(createRequest), UserProfileCore.CreateUserProfileRequest.routingKey, ctx)
      }
    }
  } ~ pathPrefix("update") {
    put {
      entity(as[UpdateUserProfileRequestBody]) { body =>
        ctx =>
          completeRequest(publisher, write(body), UserProfileCore.UpdateUserProfileRequest.routingKey, ctx)
      }
    }
  } ~ pathPrefix("delete" / Segment) { id =>
    delete { ctx =>
      val body = DeleteUserProfileRequestBody(id)
      completeRequest(publisher, write(body), UserProfileCore.DeleteUserProfileRequest.routingKey, ctx)
    }
  } ~ pathPrefix("id" / Segment) { id =>
    get { ctx =>
      val body = GetUserProfileByIdRequestBody(id)
      completeRequest(publisher, write(body), UserProfileCore.GetUserProfileByIdRequest.routingKey, ctx)
    }
  }
}
