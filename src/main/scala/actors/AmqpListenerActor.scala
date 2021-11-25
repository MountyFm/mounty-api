package actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.util.Timeout
import kz.mounty.fm.amqp.messages.AMQPMessage
import kz.mounty.fm.serializers.Serializers
import org.json4s.native.JsonMethods._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object AmqpListenerActor {
  def props()(implicit system: ActorSystem, ex: ExecutionContext): Props =
    Props(new AmqpListenerActor())
}

class AmqpListenerActor(implicit system: ActorSystem, ex: ExecutionContext)
  extends Actor
   with ActorLogging
   with Serializers {
  implicit val timeout: Timeout = 5.seconds

  override def receive: Receive = {
    case message: String =>
      log.info(s"received message $message")
      val amqpMessage = parse(message).extract[AMQPMessage]
      val actor = system.actorSelection(amqpMessage.actorPath)
      actor ! amqpMessage.entity
  }
}
