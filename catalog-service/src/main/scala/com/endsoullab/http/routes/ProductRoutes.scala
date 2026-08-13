package com.endsoullab.http.routes

import cats.data.Validated.{Invalid, Valid}
import cats.effect.IO
import cats.syntax.all.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{HttpRoutes, ParseFailure}

import java.net.URI
import com.endsoullab.core.Products
import com.endsoullab.http.responses.FailureResponse
import com.endsoullab.http.responses.ProblemDetail.*

import java.time.Instant

class ProductRoutes private (products: Products) extends Http4sDsl[IO] {
  import com.endsoullab.domain.page.*

  private object OptionalPageQueryParamMatcher
      extends OptionalValidatingQueryParamDecoderMatcher[Page]("page")

  private val getProductsRoute: HttpRoutes[IO] = HttpRoutes.of {
    case GET -> Root :? OptionalPageQueryParamMatcher(optionalPageValidated) =>
      val pageValidated = optionalPageValidated.getOrElse(Page.first.validNel[ParseFailure])

      pageValidated match {
        case Valid(page) =>
          products.get(page.toZeroBased).flatMap(Ok(_))
        case Invalid(failures) =>
          val errorDetails = failures.toList.map(_.details).mkString(", ")
          BadRequest(FailureResponse(s"유효성 검사 실패: $errorDetails"))
      }
  }

  private val findProductRoute: HttpRoutes[IO] = HttpRoutes.of { case GET -> Root / code =>
    products
      .find(code)
      .flatMap {
        case Some(product) => Ok(product)
        case None =>
          NotFound(
            ProblemDetail(
              title = "Product Not Found",
              `type` = URI.create("https://api.bookstore.com/errors/not-found"),
              status = NotFound,
              detail = s"Product not found with code $code not found".some,
              invalidParams = List(
                InvalidParam("service", "catalog-service"),
                InvalidParam("error_category", "Generic"),
                InvalidParam("timestamp", Instant.now().toString),
              )
            )
          )
      }
  }

  val routes: HttpRoutes[IO] = Router("/products" -> (getProductsRoute <+> findProductRoute))
}

object ProductRoutes {
  def apply(products: Products): ProductRoutes = new ProductRoutes(products)
}
