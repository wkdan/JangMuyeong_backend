package com.jangmuyeong.remittance.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.exception.ErrorCode;

/**
 * 전역 예외 처리기.
 * 도메인/애플리케이션 계층에서 던진 예외를 HTTP 응답으로 일관되게 변환한다.
 * 규칙:
 * - DomainException: 비즈니스 오류 → 400 또는 404로 매핑
 * - Validation 실패(@Valid): 400
 * - JSON 파싱 실패: 400
 * - 그 외 예외: 500 (서버 오류) + 로그 기록
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * 도메인 규칙 위반(비즈니스 예외) 처리.
	 *
	 * 현재 정책:
	 * - ACCOUNT_NOT_FOUND 는 리소스 부재이므로 404
	 * - 그 외 비즈니스 오류는 400
	 */
	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ErrorResponse> handleDomain(DomainException e) {
		int status = (e.getErrorCode() == ErrorCode.ACCOUNT_NOT_FOUND) ? 404 : 400;
		return ResponseEntity.status(status)
			.body(ErrorResponse.of(e.getErrorCode().name(), e.getMessage()));
	}

	/**
	 * 요청 DTO 검증 실패(@Valid) 처리
	 * - 예: amount <= 0, 필수 값 누락 등
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValid(MethodArgumentNotValidException e) {
		return ResponseEntity.badRequest()
			.body(ErrorResponse.of("VALIDATION_ERROR", "invalid request"));
	}

	/**
	 * JSON Body 파싱 실패 처리
	 * - 예: 잘못된 JSON, 타입 미스매치 등
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
		return ResponseEntity.badRequest()
			.body(ErrorResponse.of("INVALID_JSON", "invalid json body"));
	}

	/**
	 * 그 외 모든 예외(예상하지 못한 서버 오류) 처리
	 * - stacktrace 로그를 남기고 500 반환
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnknown(Exception e) {
		log.error("Unhandled exception", e);
		return ResponseEntity.status(500)
			.body(ErrorResponse.of("INTERNAL_ERROR", "internal error"));
	}
}
