package actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import kz.mounty.fm.amqp.messages.AMQPMessage
import kz.mounty.fm.domain.DomainEntity
import kz.mounty.fm.serializers.Serializers
import org.json4s.Formats
import org.json4s.jackson.Serialization

import scala.concurrent.Promise
import scala.concurrent.{ExecutionContext, Promise}

object PerRequest {
  class PerRequestActor(val routingKey: String,
                        val entity: String,
                        val promise: Promise[RouteResult],
                        val requestContext: RequestContext,
                        val publisherActor: ActorRef)
    extends PerRequest {
    val actorPath: String = self.path.toStringWithoutAddress
    val message: AMQPMessage =
      AMQPMessage(entity, routingKey, actorPath)
    publisherActor ! message
  }

}

trait PerRequest extends Actor with ActorLogging with Serializers {

  implicit val ex: ExecutionContext = context.dispatcher

  val promise: Promise[RouteResult]
  val requestContext: RequestContext

  override def receive: Receive = {
    case obj: String =>
      populateResponse(obj)
  }

  def populateResponse(obj: ToResponseMarshallable): Unit = {
    requestContext
      .complete(obj)
      .onComplete(something => promise.complete(something))

    context.stop(self)
  }

  override def postStop(): Unit =
    log.warning("I stopped and removed myself from memory")
}