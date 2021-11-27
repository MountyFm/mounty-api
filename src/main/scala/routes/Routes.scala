package routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import routes.profile.UserProfileRoutes

import scala.concurrent.{ExecutionContext, Future}

class Routes(publisher: ActorRef)(implicit ex: ExecutionContext,
               system: ActorSystem,
               timeout: Timeout) {

  val userProfileRoutes = new UserProfileRoutes(publisher).route
  def routes =
    pathPrefix("api") {
      pathPrefix("hello") {
        complete("world")
      } ~ pathPrefix("profile") {userProfileRoutes}

  }
}
