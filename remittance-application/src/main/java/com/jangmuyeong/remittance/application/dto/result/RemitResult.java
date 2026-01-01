package com.jangmuyeong.remittance.application.dto.result;

/**
 * 이체 결과
 */
public record RemitResult(
	Long fromAccountId,
	String fromAccountNo,
	Long toAccountId,
	String toAccountNo,
	long amount,
	long fee,
	long fromBalance,
	long toBalance
) {}