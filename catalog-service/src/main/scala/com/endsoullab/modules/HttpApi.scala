package com.endsoullab.modules

import cats.effect.*
import cats.implicits.* // <+>

//import org.typelevel.log4cats.Logger
import org.http4s.*
import org.http4s.server.*

import com.endsoullab.http.routes.ActuatorRoutes
import com.endsoullab.http.routes.ProductRoutes

class HttpApi private (core: Core) /*(using logger: Logger[IO])*/ {
  private val actuatorRoute = ActuatorRoutes.apply.routes
  private val productsRoute = ProductRoutes(core.products).routes

  private val apiRoutes: HttpRoutes[IO] = Router(
    "/api" -> (actuatorRoute <+> productsRoute)
  )

  val endPoints: HttpRoutes[IO] = apiRoutes
}

object HttpApi {
  def apply(core: Core): Resource[IO, HttpApi] =
    Resource.pure(new HttpApi(core))
}
