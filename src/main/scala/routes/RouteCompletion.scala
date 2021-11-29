package routes

import actors.PerRequest.PerRequestActor
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.RouteResult
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import kz.mounty.fm.domain.DomainEntity
import kz.mounty.fm.serializers.Serializers
import org.json4s.Serialization
import org.json4s.native.Serialization

import scala.concurrent.{Future, Promise}

trait RouteCompletion extends Serializers with Json4sSupport{
  implicit val serialization: Serialization = Serialization

  val exchange = "X:mounty-api-in"

  def completeRequest(publisher: ActorRef,
                      body: String,
                      routingKey: String,
                      ctx: RequestContext)
                     (implicit system: ActorSystem): Future[RouteResult] = {
    val promise = Promise[RouteResult]
    system.actorOf(
      Props(
        new PerRequestActor(
          routingKey,
          exchange,
          body,
          promise,
          ctx,
          publisher)))
    promise.future
  }
}

