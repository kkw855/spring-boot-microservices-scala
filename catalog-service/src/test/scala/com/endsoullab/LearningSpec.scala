package com.endsoullab

import cats.data.NonEmptyList
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import org.scalatest.OptionValues.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.io.{BufferedSource, Source}
import scala.sys.process.*

class LearningSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  case class Item(id: Int, title: String)

  "Learning Spec" - {
    "Source" in {
      val resource: Resource[IO, BufferedSource] = Resource.fromAutoCloseable(
        IO.blocking(Source.fromFile("catalog-service/test-data.sql", "UTF-8"))
      )

      resource.use(bufferedSource => IO.blocking(bufferedSource.getLines().toList)).asserting { lines =>
        lines should have size 28
        lines.head shouldBe "-- noinspection SqlNoDataSourceInspectionForFile"
      }
    }

    "Nel" in {
      val numbers: NonEmptyList[Int] = NonEmptyList.of(1, 2, 3, 4, 5)

      val normalList = List(1, 2, 3)
      val emptyList = List.empty[Int]

      val maybeNel1: Option[NonEmptyList[Int]] = NonEmptyList.fromList(normalList)
      val maybeNel2: Option[NonEmptyList[Int]] = NonEmptyList.fromList(emptyList)

      import cats.implicits.* // maximum

      numbers.reduce shouldBe 15
      numbers.maximum shouldBe 5

      maybeNel1 shouldBe Some(NonEmptyList.of(1, 2, 3))
      maybeNel2 shouldBe None
    }

    "math" in {
      math.ceil(2.0) shouldBe 2.0
      math.ceil(2.1) shouldBe 3.0
      math.ceil(2.5) shouldBe 3.0

      // Long / Int 같은 정수끼리의 나눗셈은 정수 나눗셈이라 소수점 이하가 그냥 버려짐
      math.ceil(25 / 10) shouldBe 2.0
      math.ceil(25.toDouble / 10) shouldBe 3.0
    }

    "Process" in {
      val ioProgram: IO[Option[String]] = IO.blocking {
        // .headOption이나 .toList처럼 실제 I/O를 수행하여 값을 확정 짓는 연산까지 IO.blocking 내부에서 처리
        Process(Seq("head", "-n", "1", ".scalafmt.conf")).lazyLines.headOption
      }

      ioProgram.asserting { line =>
        line.value should startWith("version")
      }
    }

    "Seq" in {
      val seq1 = Seq[Item](Item(1, "A"), Item(2, "B"))
      val seq2 = Seq[Item](Item(3, "C"))

      val combined = seq1 ++ seq2

      combined shouldBe Seq(Item(1, "A"), Item(2, "B"), Item(3, "C"))
    }

    "String" in {
      val commitId = "f7ec3e12446164564ed422da5908ea54746fea90"
      val shortId = commitId.substring(0, 7)

      shortId shouldBe "f7ec3e1"
    }
  }
}
