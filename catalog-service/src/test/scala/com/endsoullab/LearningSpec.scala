package com.endsoullab

import cats.data.NonEmptyList
import cats.effect.Ref
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import cats.syntax.all.*

import io.circe.Decoder
import io.circe.Encoder
import io.circe.{Codec, Json, jawn}

import org.http4s.Status

import org.scalatest.OptionValues.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.*
import scala.io.{BufferedSource, Source}
import scala.sys.process.*

class LearningSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  case class Item(id: Int, title: String)

  opaque type UserId = String
  object UserId {
    def apply(value: String): UserId = value
  }
  final case class User(id: UserId, name: String) derives Codec.AsObject
  given Codec[Status] =
    Codec.from(
      Decoder.decodeInt.emap(code => Status.fromInt(code).leftMap(_.message)),
      Encoder.encodeInt.contramap(_.code)
    )
  final case class CustomStatus(title: String, status: Status) derives Codec.AsObject

  "Learning Spec" - {
    "Circe" - {
      "기본 디코딩 인코딩" in {
        import io.circe.syntax.*

        val user = User(
          id = UserId("user-123"),
          name = "Kim"
        )
        user.asJson shouldBe Json.obj(
          "id" -> Json.fromString("user-123"),
          "name" -> Json.fromString("Kim")
        )
        jawn.decode[User]("""
          {
            "id": "user-123",
            "name": "Kim"
          }
          """) shouldBe User(UserId("user-123"), "Kim").asRight[Error]
      }

      "org.http4s.Status" in {
        import io.circe.syntax.*
        import io.circe.literal.json

        val status = CustomStatus("Ok", Status.Ok)

        status.asJson shouldBe json"""
          {
            "title": "Ok",
            "status": 200
          }
        """

        jawn.decode[CustomStatus]("""
             {
               "title": "Ok",
               "status": 200
             }
          """) shouldBe status.asRight
      }
    }

    "Cats" - {
      "Ref" - {
        def worker(id: Int, counterRef: Ref[IO, Int]): IO[Unit] =
          for {
            _ <- IO.println(s"[Fiber-$id] 작업 시작")
            _ <- IO.sleep(100.millis) // DB 저장이나 외부 API 호출 등 무거운 I/O 시뮬레이션

            _ <- counterRef.update(current => current + 1)

            _ <- IO.println(s"[Fiber-$id] 작업 완료")
          } yield ()

        "Fiber 사용해서 조회수 카운터" in {
          for {
            counter <- IO.ref(0)

            fiber1 <- worker(1, counter).start
            fiber2 <- worker(2, counter).start
            fiber3 <- worker(3, counter).start

            _ <- fiber1.join
            _ <- fiber2.join
            _ <- fiber3.join

            finalCount <- counter.get

            _ <- IO.println(s"🔥 모든 작업 종료! 최종 카운트: $finalCount")
          } yield {
            finalCount shouldBe 3
          }
        }

        "parReplicateA 사용해서 조회수 카운터" in {
          for {
            counter <- IO.ref(0)

            _ <- IO.parReplicateAN(10)(50, worker(50, counter))

            finalCount <- counter.get
          } yield {
            finalCount shouldBe 50
          }
        }
      }
    }

    "Source" in {
      val resource: Resource[IO, BufferedSource] = Resource.fromAutoCloseable(
        IO.blocking(Source.fromFile("catalog-service/test-data.sql", "UTF-8"))
      )

      resource.use(bufferedSource => IO.blocking(bufferedSource.getLines().toList)).asserting {
        lines =>
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
