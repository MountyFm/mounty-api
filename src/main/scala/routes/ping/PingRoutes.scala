package routes.ping

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import kz.mounty.fm.amqp.messages.MountyMessages.UserProfileCore
import kz.mounty.fm.amqp.messages.{MountyMessages, PingMessage}
import org.json4s.jackson.Serialization._
import routes.RouteCompletion

import scala.concurrent.ExecutionContext

class PingRoutes(publisher: ActorRef)(implicit ex: ExecutionContext,
                                      system: ActorSystem,
                                      timeout: Timeout) extends RouteCompletion {

  def route = pathPrefix("ping") {
    post {
      entity(as[PingMessage]) { body => ctx =>
        completeRequest(publisher, write(body), UserProfileCore.Ping.routingKey, ctx)
      }
    }
  }
}
