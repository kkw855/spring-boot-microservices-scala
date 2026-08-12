package com.endsoullab.core

import scala.io.Source
import java.sql.DriverManager
import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import doobie.Transactor
import doobie.util.transactor.Strategy
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.scalatest.Suite
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import org.testcontainers.utility.DockerImageName

trait DoobieSpec extends TestContainerForAll { self: Suite =>

  override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def(
    dockerImageName = DockerImageName.parse("postgres:18.4-alpine"),
    databaseName = "books_db_scala",
    username = "test_user",
    password = "test_password"
  )

  // TestContainerForAll은 스위트 전체에서 컨테이너를 하나만 띄워서 재사용하므로,
  // 스키마 마이그레이션 + 더미 데이터 시딩은 afterContainersStart에서 딱 한 번만 실행해야 함.
  override def afterContainersStart(container: PostgreSQLContainer): Unit = {
    migrateSchema(container)
    seedTestData(container).unsafeRunSync()
  }

  // 실제 운영에 쓰는 Flyway 마이그레이션을 그대로 적용해서 테스트 DB 스키마가 항상 운영과 동일하게 유지되도록 함.
  // ⚠️ target을 "1"(스키마 마이그레이션)로 고정해서 V2__add_books_data.sql(운영용 시드 데이터)은 제외함 —
  //    test-data.sql의 더미 25건과 섞이지 않게 하기 위함. 이후 순수 스키마 변경 마이그레이션(V3, V4, ...)을
  //    추가하면 이 target 버전도 같이 올려줘야 함.
  private def migrateSchema(container: PostgreSQLContainer): Unit =
    Flyway
      .configure()
      .dataSource(container.jdbcUrl, container.username, container.password)
      .locations("filesystem:catalog-service/migrations")
      .target(MigrationVersion.fromVersion("1"))
      .load()
      .migrate()

  private def seedTestData(container: PostgreSQLContainer): IO[Unit] = {
    val testDataResource = Resource.fromAutoCloseable(IO.blocking(Source.fromFile("catalog-service/test-data.sql")))
    val connectionResource = Resource.fromAutoCloseable(
      IO.blocking(DriverManager.getConnection(container.jdbcUrl, container.username, container.password))
    )

    for {
      testData <- testDataResource.use(source => IO.blocking(source.mkString))
      _ <- connectionResource.use { conn =>
        Resource.fromAutoCloseable(IO.blocking(conn.createStatement())).use { stmt =>
          IO.blocking(stmt.execute(testData))
        }
      }
    } yield ()
  }

  // 테스트 하나당 커넥션을 하나 열어서 autocommit을 끄고 Strategy.void로 doobie가
  // 알아서 begin/commit/close 하지 않게 만듦. 같은 테스트 안에서 여러 repository
  // 메서드(.transact 호출)가 전부 이 커넥션 하나를 공유해서 같은 트랜잭션 안에서 동작하고,
  // 테스트가 끝나면(성공/실패 상관없이) 항상 rollback 후 close해서 다음 테스트에 영향을 안 줌.
  protected def withTransactor[A](test: Transactor[IO] => IO[A]): IO[A] =
    withContainers { container =>
      val connectionResource = Resource.make {
        IO.blocking {
          val conn = DriverManager.getConnection(container.jdbcUrl, container.username, container.password)
          conn.setAutoCommit(false)
          conn
        }
      } { conn =>
        IO.blocking {
          conn.rollback()
          conn.close()
        }
      }

      connectionResource.use { conn =>
        val xa = Transactor.fromConnection[IO](conn, None).copy(strategy0 = Strategy.void)
        test(xa)
      }
    }
}
