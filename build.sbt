import sbtassembly.MergeStrategy

import scala.sys.process.*

// ThisBuild를 사용해 모든 하위 모듈에 Scala 버전을 전역 적용합니다.
ThisBuild / scalaVersion := "3.8.4"

val catsVersion = "2.13.0"
val catsEffectVersion = "3.7.0"
val log4catsVersion = "2.8.0"
val logbackVersion = "1.5.38"
val pureConfigVersion = "0.17.10"
val circeVersion = "0.14.16"
val circeFs2Version = "0.14.1"
val http4sVersion = "0.23.36"
val doobieVersion = "1.0.0-RC12"
val redis4cats = "2.0.5"
val testcontainersScalaVersion = "0.44.1"
val scalaTestVersion = "3.2.20"
val catsEffectTestingVersion = "1.8.0"

// 루트 프로젝트 (Maven의 packaging=pom 역할)
lazy val root = (project in file("."))
  .aggregate(catalogService /*, orderService 추후 추가 */ )
  .settings(
    name := "spring-boot-microservices-scala"
  )

// Catalog Service 마이크로서비스 모듈
lazy val catalogService = (project in file("catalog-service"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "catalog-service",
    scalacOptions ++= Seq(
      "-deprecation", // 구형 API 사용 시 상세한 경고 출력
      "-Wunused:all", // 모든 미사용 코드 감지
      "-Werror" // 경고(Warning)가 발생하면 컴파일 에러로 취급 (선택 사항)
    ),
    // 자동 생성될 스칼라 객체의 패키지명
    buildInfoPackage := "com.endsoullab",

    // 기본값인 BuildInfo 대신 원하는 객체 이름으로 변경
    buildInfoObject := "BuildInformation",

    // 기록할 빌드 정보키 설정
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      organization,
      version,
      scalaVersion,
      sbtVersion,
      // 커스텀 빌드 시각 (ISO-8601 포맷)
      "builtAt" -> java.time.Instant.now().toString
    ),
    buildInfoKeys ++= Seq[BuildInfoKey](
      "gitBranch" -> git.gitCurrentBranch.value,
      "gitCommitId" -> git.gitHeadCommit.value.map(_.substring(0, 7)).getOrElse("unknown"),
      // git log 날짜(+09:00)를 파싱하여 UTC Instant 포맷(Z)으로 변환
      "gitCommitDate" -> Process("git log -1 --format=%cI").lineStream.headOption.getOrElse("unknown"),
      "gitCommitMessage" -> git.gitHeadMessage.value.getOrElse("unknown")
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion % Runtime,
      "com.github.pureconfig" %% "pureconfig-core" % pureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-fs2" % circeFs2Version,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
      "org.typelevel" %% "cats-effect-testing-scalatest" % catsEffectTestingVersion % Test,
    ),
    Compile / run / mainClass := Some("com.endsoullab.Application"),
    Compile / run / fork := true,
    Test / fork := true,
    Test / javaOptions += "-Dapi.version=1.40",
    assembly / assemblyJarName := "app.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "maven", xs @ _*) => MergeStrategy.discard
      case PathList("META-INF", xs @ _*) =>
        xs match {
          case "MANIFEST.MF" :: Nil => MergeStrategy.discard
            // cats, http4s 등 서비스 로더 파일들을 하나로 합침
          case "services" :: _ :: Nil => MergeStrategy.concat
          case _ => MergeStrategy.first
        }
      case "reference.conf" => MergeStrategy.concat
      case x => MergeStrategy.first
    },
  )
