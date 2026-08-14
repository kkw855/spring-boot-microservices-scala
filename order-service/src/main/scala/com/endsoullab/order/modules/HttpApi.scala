package com.endsoullab.order.modules

import cats.effect.*
// import cats.implicits.* // <+>

import org.http4s.*
import org.http4s.server.*

//import org.typelevel.log4cats.Logger
import com.endsoullab.order.http.routes.ActuatorRoutes

class HttpApi private (/*core: Core*/) /*(using logger: Logger[IO])*/ {
  private val actuatorRoute = ActuatorRoutes.apply.routes
//  private val productsRoute = ProductRoutes(core.products).routes

  private val apiRoutes: HttpRoutes[IO] = Router(
    "/api" -> actuatorRoute
  )

  val endPoints: HttpRoutes[IO] = apiRoutes
}

object HttpApi {
  def apply(/*core: Core*/): Resource[IO, HttpApi] =
    Resource.pure(new HttpApi(/*core*/))
}
