package com.endsoullab

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec

import org.scalatest.OptionValues.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.sys.process.*

class LearningSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  case class Item(id: Int, title: String)

  "Learning Spec" - {
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
