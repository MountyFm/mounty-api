package actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import kz.mounty.fm.amqp.messages.AMQPMessage
import kz.mounty.fm.exceptions.ExceptionInfo
import kz.mounty.fm.serializers.Serializers
import org.json4s.JNothing
import org.json4s.jackson.JsonMethods.parse

import scala.concurrent.Promise
import scala.concurrent.ExecutionContext

object PerRequest {
  class PerRequestActor(val routingKey: String,
                        val exchange: String,
                        val entity: String,
                        val promise: Promise[RouteResult],
                        val requestContext: RequestContext,
                        val publisherActor: ActorRef)
    extends PerRequest {
    val actorPath: String = self.path.toStringWithoutAddress
    val message: AMQPMessage =
      AMQPMessage(
        entity = entity,
        exchange = exchange,
        routingKey = routingKey,
        actorPath = actorPath)
    publisherActor ! message
  }

}

trait PerRequest extends Actor with ActorLogging with Serializers {

  implicit val ex: ExecutionContext = context.dispatcher

  val promise: Promise[RouteResult]
  val requestContext: RequestContext

  override def receive: Receive = {
    case obj: String =>
      val parsedObj = parse(obj)
      val jsonClass = (parsedObj \ "jsonClass")
      if (jsonClass != JNothing && jsonClass.extract[String] == ExceptionInfo.getClass.getSimpleName.split('$').head) {
        val statusCode = (parsedObj \ "status").extract[Int]
        populateResponse(StatusCode.int2StatusCode(statusCode), obj)
      } else {
        populateResponse(StatusCode.int2StatusCode(200), obj)
      }
  }

  def populateResponse(status: StatusCode, obj: ToResponseMarshallable): Unit = {
    requestContext
      .complete(obj)
      .map {
        case successResponse: Complete =>
          successResponse.copy(response = successResponse.response.copy(status = status))
        case any => any
      }
      .onComplete(something => promise.complete(something))

    context.stop(self)
  }

  override def postStop(): Unit =
    log.warning("I stopped and removed myself from memory")
}