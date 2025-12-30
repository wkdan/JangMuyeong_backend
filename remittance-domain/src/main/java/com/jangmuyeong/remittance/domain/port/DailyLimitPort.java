package com.jangmuyeong.remittance.domain.port;

import java.time.LocalDate;

import com.jangmuyeong.remittance.domain.limit.DailyLimit;

/**
 * 일 한도 누적 저장소 포트
 */
public interface DailyLimitPort {
	DailyLimit getOrCreate(Long accountId, LocalDate date);
	DailyLimit save(DailyLimit limit);
}