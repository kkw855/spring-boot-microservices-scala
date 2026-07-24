package com.endsoullab.http.routes

import cats.effect.IO

import io.circe.Codec

import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.*
import org.http4s.server.*

import com.endsoullab.BuildInformation

final case class AppInfoResponse(
    build: AppInfoResponse.BuildInfo,
    git: AppInfoResponse.GitInfo
) derives Codec.AsObject

object AppInfoResponse {
  final case class BuildInfo(
      name: String,
      organization: String,
      version: String,
      scalaVersion: String,
      sbtVersion: String,
      builtAt: String
  ) derives Codec.AsObject

  final case class GitInfo(branch: String, commitId: String, commitDate: String)
      derives Codec.AsObject

  // BuildInfo 객체를 DTO로 변환하는 팩토리 메서드
  def fromBuildInformation: AppInfoResponse = AppInfoResponse(
    build = BuildInfo(
      name = BuildInformation.name,
      organization = BuildInformation.organization,
      version = BuildInformation.version,
      scalaVersion = BuildInformation.scalaVersion,
      sbtVersion = BuildInformation.sbtVersion,
      builtAt = BuildInformation.builtAt
    ),
    git = GitInfo(
      branch = BuildInformation.gitBranch,
      commitId = BuildInformation.gitCommitId,
      commitDate = BuildInformation.gitCommitDate
    )
  )
}

class ActuatorRoutes private extends Http4sDsl[IO] {
  private val infoRoute: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root / "info" =>
    Ok(AppInfoResponse.fromBuildInformation)
  }

  val routes: HttpRoutes[IO] = Router(
    "/" -> infoRoute
  )
}

object ActuatorRoutes {
  def apply: ActuatorRoutes = new ActuatorRoutes
}
