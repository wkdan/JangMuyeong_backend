package com.jangmuyeong.remittance.domain.exception;

/**
 * 도메인 규칙 위반 예외
 */
public class DomainException extends RuntimeException {

	private final ErrorCode errorCode;

	public DomainException(ErrorCode errorCode) {
		super(errorCode.name());
		this.errorCode = errorCode;
	}

	public DomainException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}