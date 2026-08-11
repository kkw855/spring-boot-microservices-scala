package com.endsoullab.config

import pureconfig.ConfigReader

final case class AppConfig(
    emberConfig: EmberConfig,
    postgresConfig: PostgresConfig
) derives ConfigReader
