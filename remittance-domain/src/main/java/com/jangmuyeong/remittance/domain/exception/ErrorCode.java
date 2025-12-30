package com.jangmuyeong.remittance.domain.exception;

/**
 * 도메인 에러 코드
 */
public enum ErrorCode {

	// ===== Account =====

	/** 존재하지 않는 계좌 id 조회/락 조회 시 */
	ACCOUNT_NOT_FOUND,

	/** 삭제된 계좌(DELETED)에 입금/출금/이체 등 작업 시 */
	ACCOUNT_INACTIVE,

	/** accountNo 유니크 제약 위반(계좌 등록 시 중복) */
	DUPLICATE_ACCOUNT_NO,

	// ===== Money =====

	/** 금액이 0 이하(입금/출금/이체 공통) */
	INVALID_AMOUNT,

	/** 잔액 부족으로 출금/이체 총 차감액(amount+fee)을 감당 못할 때 */
	INSUFFICIENT_BALANCE,

	// ===== Limit =====

	/** 출금 일 한도(1,000,000원/일) 초과 */
	WITHDRAW_DAILY_LIMIT_EXCEEDED,

	/** 이체 일 한도(3,000,000원/일) 초과 (이체 금액 기준) */
	TRANSFER_DAILY_LIMIT_EXCEEDED,

	// ===== Remit =====

	/** 동일 계좌(from == to)로 송금 시도 */
	SAME_ACCOUNT_TRANSFER_NOT_ALLOWED
}
