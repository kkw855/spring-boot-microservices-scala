package com.endsoullab.modules

import cats.effect.*
//import cats.implicits.*
import com.endsoullab.http.routes.ActuatorRoutes
//import org.typelevel.log4cats.Logger
import org.http4s.*
import org.http4s.server.*

class HttpApi /**/private /*(core: Core)(using logger: Logger[IO])*/ {
  private val actuatorRoute = ActuatorRoutes.apply.routes

  private val apiRoutes: HttpRoutes[IO] = Router(
    "/actuator" -> actuatorRoute
  )

  val endPoints: HttpRoutes[IO] = apiRoutes
}

object HttpApi {
  def apply: Resource[IO, HttpApi] =
    Resource.pure(new HttpApi)
}
