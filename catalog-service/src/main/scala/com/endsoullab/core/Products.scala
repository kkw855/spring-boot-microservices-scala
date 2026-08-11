// noinspection SqlNoDataSourceInspection, SqlResolve"
package com.endsoullab.core

import cats.effect.IO

import doobie.Transactor
import doobie.implicits.*

import com.endsoullab.domain.product.*

trait Products {
  def get(page: Int, limit: Int = 10): IO[List[Product]]
}

class LiveProducts private (xa: Transactor[IO]) extends Products {
  override def get(page: Int, limit: Int = 10): IO[List[Product]] = {
    sql"""
      SELECT
        id,
        code,
        name,
        description,
        image_url,
        price
      FROM products
      ORDER BY name
      LIMIT $limit
      OFFSET ${page * limit}
    """
      .query[Product]
      .to[List]
      .transact(xa)
  }
}

object LiveProducts {
  def apply(xa: Transactor[IO]): IO[LiveProducts] = IO(new LiveProducts(xa))
}
