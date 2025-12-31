package com.jangmuyeong.remittance.domain.policy;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PercentFeePolicyTest {

	@Test
	void fee_is_1_percent_floor() {
		FeePolicy policy = new PercentFeePolicy();

		assertThat(policy.calculateFee(100_000)).isEqualTo(1_000);
		assertThat(policy.calculateFee(2_000_000)).isEqualTo(20_000);

		// 정수 내림 확인 (1% = 1.01 -> 1)
		assertThat(policy.calculateFee(101)).isEqualTo(1);
	}

	@Test
	void fee_for_zero_is_zero() {
		FeePolicy policy = new PercentFeePolicy();
		assertThat(policy.calculateFee(0)).isEqualTo(0);
	}
}