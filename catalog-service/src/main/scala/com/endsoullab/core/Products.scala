// noinspection SqlNoDataSourceInspection, SqlResolve"
package com.endsoullab.core

import cats.effect.IO
import cats.implicits.*

import doobie.Transactor
import doobie.implicits.*

import com.endsoullab.domain.page.*
import com.endsoullab.domain.product.*

trait Products {
  def get(page: Int, limit: Int = 10): IO[PagedResult[Product]]
  def find(code: String): IO[Option[Product]]
}

class LiveProducts private (xa: Transactor[IO]) extends Products {
  override def get(page: Int, size: Int = 10): IO[PagedResult[Product]] = {
    val offset = page * size

    val dataQuery =
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
        LIMIT $size
        OFFSET $offset
       """
        .query[Product]
        .to[List]

    val countQuery = sql"SELECT count(*) FROM products"
      .query[Long]
      .unique

    (dataQuery, countQuery).tupled
      .transact(xa)
      .map { case (data, totalElements) =>
        PagedResult(data, totalElements, page, size)
      }
  }

  override def find(code: String): IO[Option[Product]] = {
    sql"""
      SELECT
        id,
        code,
        name,
        description,
        image_url,
        price
      FROM products
      WHERE code = $code    
    """
      .query[Product]
      .option
      .transact(xa)
  }
}

object LiveProducts {
  def apply(xa: Transactor[IO]): IO[LiveProducts] = IO(new LiveProducts(xa))
}
