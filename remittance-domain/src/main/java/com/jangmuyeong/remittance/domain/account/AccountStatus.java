package com.jangmuyeong.remittance.domain.account;

/**
 * 계좌 상태
 * ACTIVE: 정상 허용
 * DELETED: 삭제 처리(비활성, 소프트 딜리트) - 잔액 변경 및 거래 불가
 */
public enum AccountStatus {
	ACTIVE,
	DELETED
}