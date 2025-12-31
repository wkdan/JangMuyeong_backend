package com.jangmuyeong.remittance.presentation.request;

import jakarta.validation.constraints.Min;

/**
 * 금액 입력 요청 (입/출금 공용)
 */
public record AmountRequest(
	@Min(1)
	long amount
) { }