package com.endsoullab.core

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import com.endsoullab.fixtures.ProductFixture

class ProductsSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with DoobieSpec
    with ProductFixture {

  private def withProducts[A](test: Products => IO[A]): IO[A] =
    withTransactor { xa =>
      for {
        liveProducts <- LiveProducts(xa)
        result <- test(liveProducts)
      } yield result
    }

  "LiveProducts" - {
    "get" - {
      "첫 번째 페이지를 조회한다" in withProducts { products =>
        for {
          pagedResult <- products.get(0)
        } yield {
          pagedResult.data should have size 10
          pagedResult.totalElements shouldBe 25
          pagedResult.totalPages shouldBe 3
          pagedResult.pageNumber shouldBe 1
          pagedResult.isFirst shouldBe true
          pagedResult.isLast shouldBe false
          pagedResult.hasNext shouldBe true
          pagedResult.hasPrevious shouldBe false
        }
      }

      "세 번째 페이지를 조회한다" in withProducts { products =>
        for {
          pagedResult <- products.get(2)
        } yield {
          pagedResult.data should have size 5
          pagedResult.totalElements shouldBe 25
          pagedResult.totalPages shouldBe 3
          pagedResult.pageNumber shouldBe 3
          pagedResult.isFirst shouldBe false
          pagedResult.isLast shouldBe true
          pagedResult.hasNext shouldBe false
          pagedResult.hasPrevious shouldBe true
        }
      }

      "존재하지 않는 페이지를 조회한다" in withProducts { products =>
        for {
          pagedResult <- products.get(5)
        } yield {
          pagedResult.data should have size 0
          pagedResult.totalElements shouldBe 25
          pagedResult.totalPages shouldBe 3
          pagedResult.pageNumber shouldBe 6
          pagedResult.isFirst shouldBe false
          pagedResult.isLast shouldBe false
          pagedResult.hasNext shouldBe false
          pagedResult.hasPrevious shouldBe false
        }
      }
    }

    "find" - {
      "존재하는 제품 코드를 조회한다" in withProducts { products =>
        for {
          maybeProduct <- products.find("D001")
        } yield {
          maybeProduct shouldBe Some(product1)
        }
      }

      "존재하지 않는 제품 코드를 조회한다" in withProducts { products =>
        for {
          maybeProduct <- products.find("D001")
        } yield {
          maybeProduct shouldBe Some(product1)
        }
      }
    }
  }
}
