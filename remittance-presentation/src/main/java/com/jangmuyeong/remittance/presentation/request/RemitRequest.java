package com.jangmuyeong.remittance.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 이체 요청 바디
 */
public record RemitRequest(
	@NotNull Long fromAccountId,
	@NotNull Long toAccountId,
	@Min(1) long amount
) {}