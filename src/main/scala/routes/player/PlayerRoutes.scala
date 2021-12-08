package routes.player

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import kz.mounty.fm.amqp.messages.MountyMessages.RoomCore
import kz.mounty.fm.domain.commands._
import org.json4s.jackson.Serialization.write
import routes.RouteCompletion

import scala.concurrent.ExecutionContext

trait PlayerRoutes extends RouteCompletion {
  def playerRoutes(implicit ex: ExecutionContext,
                   publisher: ActorRef,
                   system: ActorSystem,
                   timeout: Timeout) = pathPrefix("playerState") {
    get {
      parameters(
        "state".as[String],
        "tokenKey".as[String],
        "deviceId".optional,
        "contextUri".as[String].optional,
        "roomId".as[String],
        "offset".as[Int].optional) { (state, tokenKey, deviceId, contextUri, roomId, offset) =>
        ctx =>
          state match {
            case "next" =>
              val bodyJson = write(NextSongCommandBody(deviceId, tokenKey, roomId))
              println(bodyJson)
              completeRequest(publisher, bodyJson, RoomCore.PlayNextTrack.routingKey, ctx)

            case "stop" =>
              val bodyJson = write(PauseSongCommandBody(deviceId, tokenKey, roomId))
              completeRequest(publisher, bodyJson, RoomCore.PauseSong.routingKey, ctx)

            case "prev" =>
              val bodyJson = write(PrevSongCommandBody(deviceId, tokenKey, roomId))
              completeRequest(publisher, bodyJson, RoomCore.PlayPrevTrack.routingKey, ctx)
            case "play" =>
              val bodyJson = write(PlaySongCommandBody(
                contextUri = contextUri,
                offset = offset,
                tokenKey = tokenKey,
                roomId = roomId,
                deviceId = deviceId))
              completeRequest(publisher, bodyJson, RoomCore.PlaySong.routingKey, ctx)
          }
      }
    }
  }
}
