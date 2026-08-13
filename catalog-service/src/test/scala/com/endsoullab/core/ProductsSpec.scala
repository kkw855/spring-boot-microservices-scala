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
      "0번째 페이지를 조회하면 처음 10건을 반환한다" in withProducts { products =>
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

      "마지막 페이지를 조회하면 남은 5건만 반환한다" in withProducts { products =>
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

      "범위를 벗어난 페이지를 조회하면 빈 목록을 반환한다" in withProducts { products =>
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
      "존재하는 제품 코드로 조회하면 해당 제품을 반환한다" in withProducts { products =>
        for {
          maybeProduct <- products.find("D001")
        } yield {
          maybeProduct shouldBe Some(product1)
        }
      }

      "존재하지 않는 제품 코드로 조회하면 None을 반환한다" in withProducts { products =>
        for {
          maybeProduct <- products.find("NOT_EXIST")
        } yield {
          maybeProduct shouldBe None
        }
      }
    }
  }
}
