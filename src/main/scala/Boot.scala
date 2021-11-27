import actors.{AmqpListenerActor, AmqpPublisherActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import kz.mounty.fm.amqp.{AmqpConsumer, RabbitMQConnection}
import org.slf4j.LoggerFactory
import routes.Routes

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object Boot extends App {
  implicit val system: ActorSystem = ActorSystem("mounty-api")
  implicit val materializer = Materializer(system)
  implicit val ex: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(60.seconds)
  val logger = LoggerFactory.getLogger("")

  val config = ConfigFactory.load()
  val rmqHost = config.getString("rabbitmq.host")
  val rmqPort = config.getInt("rabbitmq.port")
  val username = config.getString("rabbitmq.username")
  val password = config.getString("rabbitmq.password")
  val virtualHost = config.getString("rabbitmq.virtualHost")

  val connection = RabbitMQConnection.rabbitMQConnection(
    username,
    password,
    rmqHost,
    rmqPort,
    virtualHost
  )

  val channel = connection.createChannel()

  RabbitMQConnection.declareExchange(
    channel,
    "X:mounty-api-out",
    "topic") match {
    case Success(value) => system.log.info("succesfully declared exchange")
    case Failure(exception) => system.log.warning(s"couldn't declare exchange ${exception.getMessage}")
  }

  RabbitMQConnection.declareAndBindQueue(
    channel,
    "Q:mounty-api-queue",
    "X:mounty-api-out",
    "mounty-messages.mounty-api.#"
  )

  val publisher: ActorRef = system.actorOf(
    AmqpPublisherActor.props(channel, "X:mounty-api-in"))

  val listener: ActorRef = system.actorOf(AmqpListenerActor.props())
  channel.basicConsume("Q:mounty-api-queue", AmqpConsumer(listener))

  val routes = new Routes(publisher)

  Http()
    .newServerAt("localhost", 8080)
    .bind(routes.routes)
}
