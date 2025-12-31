package com.jangmuyeong.remittance.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.exception.ErrorCode;
import com.jangmuyeong.remittance.domain.ledger.LedgerEntry;
import com.jangmuyeong.remittance.domain.port.AccountPort;
import com.jangmuyeong.remittance.domain.port.LedgerPort;

/**
 * 거래 내역 조회 애플리케이션 서비스
 */
@Service
public class TransactionQueryService {

	private final AccountPort accountPort;
	private final LedgerPort ledgerPort;

	public TransactionQueryService(AccountPort accountPort, LedgerPort ledgerPort) {
		this.accountPort = accountPort;
		this.ledgerPort = ledgerPort;
	}

	/**
	 * 계좌별 최신 거래내역 조회
	 * @param accountId 계좌 id
	 * @param size 반환 개수
	 */
	@Transactional(readOnly = true)
	public List<LedgerEntry> latest(Long accountId, int size) {
		accountPort.findById(accountId)
			.orElseThrow(() -> new DomainException(ErrorCode.ACCOUNT_NOT_FOUND));
		// 요구사항: 최신순
		return ledgerPort.findLatestByAccountId(accountId, size);
	}
}
