package com.endsoullab.config

import pureconfig.ConfigReader

final case class AppConfig(
    emberConfig: EmberConfig
) derives ConfigReader
