package com.jangmuyeong.remittance.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 이체 요청 바디
 */
public record RemitRequest(
	@NotBlank String fromAccountNo,
	@NotBlank String toAccountNo,
	@Min(1) long amount
) {}