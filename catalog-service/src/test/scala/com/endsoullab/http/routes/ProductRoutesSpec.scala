package com.endsoullab.http.routes

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Ref}

import org.http4s.circe.CirceEntityCodec.*
import org.http4s.implicits.*
import org.http4s.{Method, Request, Status}

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import com.endsoullab.core.Products
import com.endsoullab.domain.page.*
import com.endsoullab.domain.product.*
import com.endsoullab.fixtures.ProductFixture
import com.endsoullab.http.responses.FailureResponse

final case class PaginationArgs(page: Int, limit: Int)

class ProductRoutesSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with ProductFixture {
  class ProductsStub(val getHistoryRef: Ref[IO, PaginationArgs]) extends Products {
    override def get(page: Int, limit: Int = 10): IO[PagedResult[Product]] = {
      val pagedResultIO =
        if (page == 0 && limit > 0)
          IO.pure(
            PagedResult(
              data = List(product1),
              totalElements = 1,
              page = page,
              size = limit
            )
          )
        else
          IO.pure(
            PagedResult(
              data = List.empty[Product],
              totalElements = 1,
              page = page,
              size = limit
            )
          )

      getHistoryRef.set(PaginationArgs(page, limit)) >> pagedResultIO
    }

    override def find(code: String): IO[Option[Product]] = ???
  }

  private def badRequestFor(pageValue: String, expectedError: String) =
    for {
      getHistoryRef <- IO.ref(PaginationArgs(0, 0))
      productRoutes <- IO(ProductRoutes(new ProductsStub(getHistoryRef)).routes)
      response <- productRoutes.orNotFound.run(
        Request(method = Method.GET, uri = uri"/products".withQueryParam("page", pageValue))
      )
      retrieved <- response.as[FailureResponse]
    } yield {
      response.status shouldBe Status.BadRequest
      retrieved.error should include(expectedError)
    }

  "ProductRoutes" - {
    "GET /products" - {
      "정상적으로 첫 번째 페이지를 조회한다" in {
        for {
          getHistoryRef <- IO.ref(PaginationArgs(0, 0))
          productRoutes <- IO(ProductRoutes(new ProductsStub(getHistoryRef)).routes)
          response <- productRoutes.orNotFound.run(
            Request(method = Method.GET, uri = uri"/products")
          )
          retrieved <- response.as[PagedResult[Product]]
          getHistory <- getHistoryRef.get
        } yield {
          response.status shouldBe Status.Ok
          getHistory shouldBe PaginationArgs(0, 10)
          retrieved.data shouldBe List(product1)
        }
      }

      "page 파라미터가 1보다 작으면 400 Bad Request를 반환한다" in
        badRequestFor("-1", "page 파라미터는 1 이상의 양수여야 합니다")

      "page 파라미터가 정수가 아니면 400 Bad Request를 반환한다" in
        badRequestFor("a1b2c", "page 파라미터는 정수(Int) 형식이어야 합니다")
    }
    
    "GET /products/{id}" - {
      
    }
  }
}
