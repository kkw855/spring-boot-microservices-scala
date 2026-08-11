package com.endsoullab.domain

import io.circe.Codec

object product {
  final case class Product(
      id: Long,
      code: String,
      name: String,
      description: Option[String],
      imageUrl: Option[String],
      price: Double
  ) derives Codec.AsObject
}
