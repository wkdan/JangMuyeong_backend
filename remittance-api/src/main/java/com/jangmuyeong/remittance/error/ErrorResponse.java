package com.jangmuyeong.remittance.error;

/**
 * API 공통 에러 응답 모델
 * 응답 예시:
 * {
 *   "code": "ACCOUNT_NOT_FOUND",
 *   "message": "ACCOUNT_NOT_FOUND"
 * }
 */
public record ErrorResponse(String code, String message) {

	/**
	 * 팩토리 메서드.
	 * 컨트롤러/핸들러에서 new를 반복하지 않도록 제공
	 */
	public static ErrorResponse of(String code, String message) {
		return new ErrorResponse(code, message);
	}
}