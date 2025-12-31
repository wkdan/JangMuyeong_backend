package com.jangmuyeong.remittance.application.dto.result;

/**
 * 이체 결과
 */
public record RemitResult(
	Long fromAccountId,
	Long toAccountId,
	long amount,
	long fee,
	long fromBalance,
	long toBalance
) {}