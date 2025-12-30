package com.jangmuyeong.remittance.domain.port;

import java.util.List;

import com.jangmuyeong.remittance.domain.ledger.LedgerEntry;

/**
 * 거래 내역 저장소 포트 (최신순 조회)
 */
public interface LedgerPort {
	LedgerEntry save(LedgerEntry entry);

	// 최신순 조회
	List<LedgerEntry> findLatestByAccountId(Long accountId, int size);
}