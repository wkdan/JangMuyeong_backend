package com.jangmuyeong.remittance.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jangmuyeong.remittance.application.dto.command.DepositCommand;
import com.jangmuyeong.remittance.application.dto.command.WithdrawCommand;
import com.jangmuyeong.remittance.application.dto.result.BalanceResult;
import com.jangmuyeong.remittance.domain.account.Account;
import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.exception.ErrorCode;
import com.jangmuyeong.remittance.domain.ledger.LedgerEntry;
import com.jangmuyeong.remittance.domain.ledger.TransactionType;
import com.jangmuyeong.remittance.domain.limit.DailyLimit;
import com.jangmuyeong.remittance.domain.port.AccountPort;
import com.jangmuyeong.remittance.domain.port.DailyLimitPort;
import com.jangmuyeong.remittance.domain.port.LedgerPort;

/**
 * 입출금 애플리케이션 서비스
 */
@Service
public class MoneyService {

	private final AccountPort accountPort;
	private final DailyLimitPort dailyLimitPort;
	private final LedgerPort ledgerPort;
	private final Clock clock;

	public MoneyService(AccountPort accountPort, DailyLimitPort dailyLimitPort, LedgerPort ledgerPort, Clock clock) {
		this.accountPort = accountPort;
		this.dailyLimitPort = dailyLimitPort;
		this.ledgerPort = ledgerPort;
		this.clock = clock;
	}

	/**
	 * 입금
	 */
	@Transactional
	public BalanceResult deposit(DepositCommand command) {
		// 잔액 변경이므로 for update 락 조회
		Account account = accountPort.findByIdForUpdate(command.accountId())
			.orElseThrow(() -> new DomainException(ErrorCode.ACCOUNT_NOT_FOUND));

		account.deposit(command.amount());
		Account saved = accountPort.save(account);

		// 입금 원장 기록
		ledgerPort.save(new LedgerEntry(null, saved.getId(), null, TransactionType.DEPOSIT,
			command.amount(), 0L, Instant.now(clock)));

		return new BalanceResult(saved.getId(), saved.getBalance());
	}

	/**
	 * 출금
	 */
	@Transactional
	public BalanceResult withdraw(WithdrawCommand command) {
		Account account = accountPort.findByIdForUpdate(command.accountId())
			.orElseThrow(() -> new DomainException(ErrorCode.ACCOUNT_NOT_FOUND));

		// 요구사항: 출금 일 한도 1,000,000원
		DailyLimit limit = dailyLimitPort.getOrCreate(account.getId(), LocalDate.now(clock));
		limit.addWithdraw(command.amount());
		dailyLimitPort.save(limit);

		account.withdraw(command.amount());
		Account saved = accountPort.save(account);

		// 출금 원장 기록
		ledgerPort.save(new LedgerEntry(null, saved.getId(), null, TransactionType.WITHDRAW,
			command.amount(), 0L, Instant.now(clock)));

		return new BalanceResult(saved.getId(), saved.getBalance());
	}
}
