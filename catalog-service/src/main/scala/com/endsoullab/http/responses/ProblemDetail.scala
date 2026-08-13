package com.endsoullab.http.responses

import io.circe.Codec
import io.circe.Decoder
import io.circe.Encoder
import cats.syntax.all.*
import org.http4s.Status

import java.net.URI

object ProblemDetail {
  /** 📌 RFC 7807 / RFC 9457 표준 에러 응답 모델 (Problem Detail ADT)
   *
   * @param type
   * 에러 유형을 식별하는 URI (기본값: "about:blank")
   * @param title
   * 사람이 읽을 수 있는 에러의 요약 제목
   * @param status
   * HTTP 상태 코드 (Http4s Status 객체)
   * @param detail
   * 상세 에러 설명 (선택)
   * @param instance
   * 문제가 발생한 특정 리소스의 URI (선택)
   * @param invalidParams
   * 폼/쿼리 파라미터 유효성 검증 실패 세부 정보 (확장 필드)
   */
  final case class ProblemDetail(
                                  title: String,
                                  status: Status,
                                  `type`: URI = URI.create("about:blank"),
                                  detail: Option[String] = None,
                                  instance: Option[URI] = None,
                                  invalidParams: List[InvalidParam] = List.empty
                                ) derives Codec.AsObject
  
  final case class InvalidParam(name: String, reason: String) derives Codec.AsObject

  given Codec[Status] =
    Codec.from(
      Decoder.decodeInt.emap(code => Status.fromInt(code).leftMap(_.message)),
      Encoder.encodeInt.contramap(_.code)
    )
}
