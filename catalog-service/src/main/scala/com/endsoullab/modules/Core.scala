package com.endsoullab.modules

import cats.effect.{IO, Resource}

class Core {}

object Core {
  def apply(): Resource[IO, Core] = {
    Resource.eval(IO(new Core))
  }
}
