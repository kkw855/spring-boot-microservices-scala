package com.endsoullab.order

import cats.effect.{IO, IOApp, Resource}

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax.*

import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

import com.endsoullab.order.config.AppConfig
import com.endsoullab.order.modules.{Core, Database, HttpApi}

object OrderApplication extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    ConfigSource.default.loadF[IO, AppConfig]().flatMap {
      case AppConfig(emberConfig, postgresConfig) =>
        val appResource: Resource[IO, Server] = for {
          _ <- Database.makePostgresResource(postgresConfig)
          _ <- Core(/*xa*/)
          httpApi <- HttpApi(/*core*/)
          server <- EmberServerBuilder
            .default[IO]
            .withHost(emberConfig.host)
            .withPort(emberConfig.port)
            .withHttpApp(httpApi.endPoints.orNotFound)
            .build
        } yield server

        appResource
          .evalTap(server => logger.info(s"Server started on PORT: ${server.address.getPort}"))
          .useForever
    }
}
