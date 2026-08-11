package com.endsoullab.http.routes

import cats.effect.IO

import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.* // http4s Status
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.server.Router

import com.endsoullab.core.Products

object PageQueryParameter extends OptionalQueryParamDecoderMatcher[Int]("page")

class ProductRoutes private (products: Products) extends Http4sDsl[IO] {
  private val getProductsRoute: HttpRoutes[IO] = HttpRoutes.of {
    case GET -> Root :? PageQueryParameter(page) =>
      val pageNo = page.map(_ - 1).getOrElse(1)
      
      products.get(pageNo).flatMap(Ok(_))
  }

  val routes: HttpRoutes[IO] = Router("/products" -> getProductsRoute)
}

object ProductRoutes {
  def apply(products: Products): ProductRoutes = new ProductRoutes(products)
}
