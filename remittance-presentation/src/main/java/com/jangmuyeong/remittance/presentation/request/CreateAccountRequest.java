package com.jangmuyeong.remittance.presentation.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 계좌 생성 요청 바디
 */
public record CreateAccountRequest(
	@NotBlank
	String accountNo
) {}