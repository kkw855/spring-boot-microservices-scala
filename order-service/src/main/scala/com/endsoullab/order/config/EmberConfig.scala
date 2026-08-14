package com.endsoullab.order.config

import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

final case class EmberConfig(host: Host, port: Port) derives ConfigReader

object EmberConfig {
  given hostReader: ConfigReader[Host] = ConfigReader[String].emap { hostString =>
    Host
      .fromString(hostString)
      .toRight(
        CannotConvert(hostString, "Host", s"Invalid host string: $hostString")
      )
  }

  given portReader: ConfigReader[Port] = ConfigReader[Int].emap { portInt =>
    Port
      .fromInt(portInt)
      .toRight(
        CannotConvert(portInt.toString, "Port", s"Invalid port number: $portInt")
      )
  }
}
