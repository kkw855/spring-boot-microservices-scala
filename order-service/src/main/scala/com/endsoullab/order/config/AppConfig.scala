package com.endsoullab.order.config

import pureconfig.ConfigReader

final case class AppConfig(
    emberConfig: EmberConfig,
    postgresConfig: PostgresConfig
) derives ConfigReader
