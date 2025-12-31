package com.jangmuyeong.remittance.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jangmuyeong.remittance.domain.policy.FeePolicy;
import com.jangmuyeong.remittance.domain.policy.PercentFeePolicy;

/**
 * 수수료 정책: 이체 금액의 1% (정수 내림)
 */
@Configuration
public class PolicyConfig {

	@Bean
	public FeePolicy feePolicy() {
		return new PercentFeePolicy();
	}
}