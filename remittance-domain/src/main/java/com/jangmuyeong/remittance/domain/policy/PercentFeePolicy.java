package com.jangmuyeong.remittance.domain.policy;

/**
 * 요구사항: 이체 금액의 1% 수수료(정수 내림)
 */
public class PercentFeePolicy implements FeePolicy {
	@Override
	public long calculateFee(long amount) {
		return (amount * 1L) / 100L;
	}
}
