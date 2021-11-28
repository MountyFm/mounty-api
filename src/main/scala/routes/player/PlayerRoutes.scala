package routes.player

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import kz.mounty.fm.amqp.messages.MountyMessages.{RoomCore, SpotifyGateway, UserProfileCore}
import kz.mounty.fm.domain.commands._
import org.json4s.jackson.Serialization.write
import routes.RouteCompletion

import scala.concurrent.ExecutionContext

trait PlayerRoutes extends RouteCompletion{
  val exchange = "X:mounty-api-in"
  def playerRoutes(implicit ex: ExecutionContext,
                   publisher: ActorRef,
                   system: ActorSystem,
                   timeout: Timeout) = pathPrefix("playerState") {
    put {
      parameters(
        "state".as[String],
        "tokenKey".as[String],
        "deviceId".optional,
        "contextUri".as[String].optional,
        "roomId".as[String],
        "offset".as[Int].optional) { (state,tokenKey, deviceId, contextUri, roomId, offset) => ctx =>
        state match {
          case "next" =>
            val body = write(NextSongCommandBody(deviceId, tokenKey, roomId))
            completeRequest(publisher, write(body), RoomCore.PlayNextTrack.routingKey, exchange,  ctx)

          case "stop" =>
            val body = write(PauseSongCommandBody(deviceId, tokenKey, roomId))
            completeRequest(publisher, write(body), RoomCore.PauseSong.routingKey, exchange,  ctx)

          case "prev" =>
            val body = write(PrevSongCommandBody(deviceId, tokenKey, roomId))
            completeRequest(publisher, write(body), RoomCore.PlayPrevTrack.routingKey, exchange,  ctx)
          case "play" =>
            val body = write(PlaySongCommandBody(
              contextUri = contextUri,
              offset = offset,
              tokenKey = tokenKey,
              roomId = roomId,
              deviceId = deviceId))
            completeRequest(publisher, body, RoomCore.PlaySong.routingKey, exchange, ctx)
        }
      }
    }
  }
}
