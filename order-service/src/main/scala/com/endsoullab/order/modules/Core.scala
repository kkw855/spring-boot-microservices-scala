package com.endsoullab.order.modules

import cats.effect.{IO, Resource}

//import com.endsoullab.core.{LiveProducts, Products}
//import doobie.util.transactor.Transactor

class Core private {}

object Core {
  def apply(/*xa: Transactor[IO]*/): Resource[IO, Core] = {
//    val coreIO = for {
//      liveProducts <- LiveProducts(xa)
//    } yield new Core(liveProducts)

//    Resource.eval(coreIO)
    Resource.pure(new Core)
  }
}
