package com.endsoullab.http.routes

import cats.data.Validated.{Invalid, Valid}
import cats.effect.IO
import cats.syntax.all.* // asLeft

import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder}

import com.endsoullab.core.Products

class ProductRoutes private (products: Products) extends Http4sDsl[IO] {
  implicit val pageQueryParamDecoder: QueryParamDecoder[Int] = QueryParamDecoder[String].emap {
    rawValue =>
      rawValue.toIntOption match {
        case None =>
          ParseFailure(
            sanitized = "잘못된 쿼리 파라미터 타입",
            details = s"page 파라미터는 정수(Int) 형식이어야 합니다. 입력값: '$rawValue'"
          ).asLeft[Int]
        case Some(page) if page < 1 =>
          ParseFailure(
            sanitized = "잘못된 쿼리 파라미터 범위",
            details = s"page 파라미터는 1 이상의 양수여야 합니다. 입력값: $page"
          ).asLeft[Int]
        case Some(page) =>
          page.asRight[ParseFailure]
      }
  }

  private object OptionalPageQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Int]("page")

  private val getProductsRoute: HttpRoutes[IO] = HttpRoutes.of {
    case GET -> Root :? OptionalPageQueryParamMatcher(optionalPageValidated) =>
      val pageValidated = optionalPageValidated.getOrElse(Valid(1))

      pageValidated match {
        case Valid(page) =>
          products.get(page - 1).flatMap(Ok(_))
        case Invalid(failures) =>
          val errorDetails = failures.toList.map(_.details).mkString(", ")
          BadRequest(s"유효성 검사 실패: $errorDetails")
      }
  }

  val routes: HttpRoutes[IO] = Router("/products" -> getProductsRoute)
}

object ProductRoutes {
  def apply(products: Products): ProductRoutes = new ProductRoutes(products)
}
