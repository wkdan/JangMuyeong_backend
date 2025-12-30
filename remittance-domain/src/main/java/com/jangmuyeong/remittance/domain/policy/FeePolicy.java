package com.jangmuyeong.remittance.domain.policy;

/**
 * 수수료 계산 전략
 */
public interface FeePolicy {
	/**
	 * @param amount 이체 금액
	 * @return 수수료(원 단위)
	 */
	long calculateFee(long amount);
}
