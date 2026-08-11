package com.endsoullab.modules

import cats.effect.{IO, Resource}

import doobie.util.transactor.Transactor

import com.endsoullab.core.{LiveProducts, Products}

class Core private (val products: Products) {}

object Core {
  def apply(xa: Transactor[IO]): Resource[IO, Core] = {
    val coreIO = for {
      liveProducts <- LiveProducts(xa)
    } yield new Core(liveProducts)

    Resource.eval(coreIO)
  }
}
