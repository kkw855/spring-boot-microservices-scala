package com.endsoullab.http.routes

import cats.effect.IO

import io.circe.Codec

import org.http4s.*
import org.http4s.circe.CirceEntityCodec.* // Case Class를 JSON HTTP 응답(EntityEncoder)으로 자동 변환
import org.http4s.dsl.*
import org.http4s.server.*

import com.endsoullab.BuildInfo

final case class AppInfoResponse(
    name: String,
    organization: String,
    version: String,
    scalaVersion: String,
    sbtVersion: String,
    builtAt: String
) derives Codec.AsObject

object AppInfoResponse {
  // BuildInfo 객체를 DTO로 변환하는 팩토리 메서드
  def fromBuildInfo: AppInfoResponse = AppInfoResponse(
    name = BuildInfo.name,
    organization = BuildInfo.organization,
    version = BuildInfo.version,
    scalaVersion = BuildInfo.scalaVersion,
    sbtVersion = BuildInfo.sbtVersion,
    builtAt = BuildInfo.builtAt // 커스텀 키
  )
}

class ActuatorRoutes private extends Http4sDsl[IO] {
  private val infoRoute: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root / "info" =>
    Ok(AppInfoResponse.fromBuildInfo)
  }

  val routes: HttpRoutes[IO] = Router(
    "/" -> infoRoute
  )
}

object ActuatorRoutes {
  def apply: ActuatorRoutes = new ActuatorRoutes
}
