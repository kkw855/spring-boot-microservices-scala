package com.endsoullab.domain

import io.circe.Codec

final case class PagedResult[T] private (
    data: List[T],
    totalElements: Long,
    pageNumber: Int,
    totalPages: Int,
    isFirst: Boolean,
    isLast: Boolean,
    hasNext: Boolean,
    hasPrevious: Boolean
) derives Codec.AsObject

object PagedResult {
  def apply[T](data: List[T], totalElements: Long, page: Int, size: Int): PagedResult[T] = {
    val totalPages = if (size <= 0) 0 else math.ceil(totalElements.toDouble / size).toInt

    PagedResult(
      data = data,
      totalElements = totalElements,
      pageNumber = page + 1,
      totalPages = totalPages,
      isFirst = page == 0,
      isLast = page == totalPages - 1,
      hasNext = page < totalPages - 1,
      hasPrevious = page > 0
    )
  }
}
