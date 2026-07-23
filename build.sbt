scalaVersion := "3.8.4"

// 루트 프로젝트 (Maven의 packaging=pom 역할)
lazy val root = (project in file("."))
  .aggregate(catalogService /*, orderService 추후 추가 */)
  .settings(
    name := "spring-boot-microservices-course"
  )

// Catalog Service 마이크로서비스 모듈
lazy val catalogService = (project in file("catalog-service"))
  .settings(
    name := "catalog-service",
    libraryDependencies ++= Seq(
      // catalog-service 전용 라이브러리들 (Cats Effect, Http4s 등)
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "org.http4s"     %% "http4s-ember-server" % "0.23.27"
    )
  )
