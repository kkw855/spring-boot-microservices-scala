package com.endsoullab.order.modules

import cats.effect.{IO, Resource}

import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

import com.endsoullab.order.config.PostgresConfig

object Database {
  def makePostgresResource(config: PostgresConfig): Resource[IO, HikariTransactor[IO]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool(config.nThreads)
      xa <- HikariTransactor.initial[IO](ec).evalMap { xa =>
        xa.configure { ds =>
          IO {
            ds.setDriverClassName("org.postgresql.Driver")
            ds.setJdbcUrl(config.url)
            ds.setUsername(config.user)
            ds.setPassword(config.pass)
            ds.setMaximumPoolSize(config.nThreads) // 스레드 수와 풀 크기 매칭
            ds.setMinimumIdle(config.nThreads / 4) // 유휴 커넥션 최소 수 유지
            ds.setIdleTimeout(600000)
            ds.setConnectionTimeout(30000)
          }.as(xa)
        }
      }
    } yield xa
}
