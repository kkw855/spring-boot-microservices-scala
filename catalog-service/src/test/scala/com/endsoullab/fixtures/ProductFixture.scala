package com.endsoullab.fixtures

import cats.syntax.option.*

import com.endsoullab.domain.product.*

trait ProductFixture {
  val product1 = Product(
    id = 1,
    code = "D001",
    name = "Dummy Product 01",
    description = "Description for dummy product 01".some,
    imageUrl = "https://example.com/images/d001.jpg".some,
    price = 10.00
  )
}
