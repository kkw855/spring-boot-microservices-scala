import cats.effect.*

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax.*

import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

import com.endsoullab.config.AppConfig
import com.endsoullab.modules.{Core, Database, HttpApi}

object Application extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    ConfigSource.default.loadF[IO, AppConfig]().flatMap {
      case AppConfig(emberConfig, postgresConfig) =>
        val appResource: Resource[IO, Server] = for {
          xa <- Database.makePostgresResource(postgresConfig)
          core <- Core(xa)
          httpApi <- HttpApi(core)
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
