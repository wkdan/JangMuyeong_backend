package com.jangmuyeong.remittance.domain.ledger;

/**
 * 거래 타입
 * - DEPOSIT/WITHDRAW: 입금/출금
 * - TRANSFER_OUT/TRANSFER_IN: 송금/수취
 * - FEE: 수수료(송금 계좌에만 발생)
 */
public enum TransactionType {
	DEPOSIT,
	WITHDRAW,
	TRANSFER_OUT,
	TRANSFER_IN,
	FEE
}
