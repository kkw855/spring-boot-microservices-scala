package com.endsoullab.domain

import cats.syntax.all.*

import io.circe.Codec

import org.http4s.{ParseFailure, QueryParamDecoder}

object page {
  opaque type Page = Int

  object Page {
    sealed trait INVALID_PAGE(val message: String)
    private final case class INVALID_PAGE_TYPE(value: String)
        extends INVALID_PAGE(s"page 파라미터는 정수(Int) 형식이어야 합니다. 입력값: '$value'")
    private final case class INVALID_PAGE_RANGE(number: Int)
        extends INVALID_PAGE(s"page 파라미터는 1 이상의 양수여야 합니다. 입력값: $number")

    val first: Page = 1
    
    def apply(value: Int): Either[INVALID_PAGE, Page] =
      if (value >= 1)
        value.asRight
      else
        INVALID_PAGE_RANGE(value).asLeft

    def fromString(s: String): Either[INVALID_PAGE, Page] =
      s.toIntOption match {
        case Some(str) => apply(str)
        case None      => INVALID_PAGE_TYPE(s).asLeft
      }
      
    extension (page: Page)
      def toZeroBased: Int = page - 1  
  }

  given pageQueryParamDecoder: QueryParamDecoder[Page] = QueryParamDecoder[String].emap {
    rawValue =>
      Page.fromString(rawValue).leftMap { invalid =>
        ParseFailure(
          sanitized = "잘못된 page 쿼리 파라미터",
          details = invalid.message
        )
      }
  }

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
        hasPrevious = page > 0 && page < totalPages
      )
    }
  }
}
