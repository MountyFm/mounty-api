package routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}
import akka.util.Timeout
import kz.mounty.fm.serializers.Serializers
import routes.ping.PingRoutes

import scala.concurrent.{ExecutionContext, Future}

class Routes(publisher: ActorRef)(implicit ex: ExecutionContext,
               system: ActorSystem,
               timeout: Timeout) {

  val pingRoutes = new PingRoutes(publisher).route
  def routes =
    pathPrefix("api") {
      pathPrefix("hello") {
        complete("world")
      } ~ pingRoutes

  }
}
