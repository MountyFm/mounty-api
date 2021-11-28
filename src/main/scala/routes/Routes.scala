package routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import routes.profile.UserProfileRoutes

import scala.concurrent.{ExecutionContext, Future}

class Routes(implicit ex: ExecutionContext,
             publisher: ActorRef,
               system: ActorSystem,
               timeout: Timeout)
  extends UserProfileRoutes {


  def routes =
    pathPrefix("api") {
      pathPrefix("hello") {
        complete("world")
      } ~ pathPrefix("profile") {userProfileRoutes}

  }
}
