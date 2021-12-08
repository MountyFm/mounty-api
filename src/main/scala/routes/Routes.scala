package routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import routes.player.PlayerRoutes
import routes.profile.UserProfileRoutes
import routes.room.RoomRoutes
import routes.roomuser.RoomUserRoutes
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.model.{HttpHeaderRange, HttpOriginMatcher}
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.concurrent.{ExecutionContext, Future}

class Routes(implicit ex: ExecutionContext,
             publisher: ActorRef,
             system: ActorSystem,
             timeout: Timeout)
  extends UserProfileRoutes
    with PlayerRoutes
    with RoomRoutes
    with RoomUserRoutes {

  val settings = CorsSettings(system)
    .withAllowedOrigins(HttpOriginMatcher.*)
    .withAllowedHeaders(HttpHeaderRange.*)

  def routes = cors(settings) {
    pathPrefix("api") {
      pathPrefix("hello") {
        complete("world")
      } ~ pathPrefix("profile") {
        userProfileRoutes
      } ~ pathPrefix("player") {
        playerRoutes
      } ~ pathPrefix("rooms") {
        roomRoutes
      } ~ pathPrefix("room-users") {
        roomUserRoutes
      }
    }
  }
}
