# syntax=docker/dockerfile:1

# ==========================================
# 1단계: 빌드 스테이지 (SBT & JDK 포함)
# ==========================================
# 사용 중인 Java 및 스칼라 버전과 매칭되는 공식 sbt 이미지 사용
FROM sbtscala/scala-sbt:eclipse-temurin-21.0.11_10_1.x AS builder

WORKDIR /app

# [오적화 팁] 의존성 라이브러리 캐싱을 위해 빌드 파일 먼저 복사
COPY project/plugins.sbt project/build.properties /app/project/
COPY build.sbt /app/

# 소스코드가 없어도 의존성을 미리 다운로드하여 빌드 속도 향상 (Docker 레이어 캐시 활용)
RUN sbt update

# 나머지 소스코드 복사 및 단일 Fat JAR 파일 생성
COPY . /app
RUN sbt "orderService/assembly"

# ==========================================
# 2단계: 런타임 스테이지 (최소한의 가벼운 JRE만 포함)
# ==========================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 보안 가이드: 프로덕션에서는 절대 root 계정으로 컨테이너를 구동하지 않습니다.
RUN useradd -u 1001 scala-user
USER scala-user

# 1단계 빌드 컴파일 결과물(Fat JAR)만 가볍게 복사
# scala-3.8.4 대신 scala-3.* 와일드카드
COPY --from=builder --chown=scala-user:scala-user /app/order-service/target/scala-3.*/app.jar ./app.jar

# 쿠버네티스 컨테이너 환경을 위한 필수 JVM 최적화 옵션 설정
# - MaxRAMPercentage: 쿠버네티스 파드 메모리 Limit의 75%를 JVM 힙 메모리로 할당 (OOMKilled 방지)
ENV JAVA_OPTS="-XX:+ExitOnOutOfMemoryError \
               -XX:InitialRAMPercentage=50.0 \
               -XX:MaxRAMPercentage=75.0"

# 애플리케이션 포트 개방 (예: Http4s 서버 포트)
EXPOSE 5022

# 실행 명령
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
