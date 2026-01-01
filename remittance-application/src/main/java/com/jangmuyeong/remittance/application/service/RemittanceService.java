package com.jangmuyeong.remittance.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jangmuyeong.remittance.application.dto.command.RemitCommand;
import com.jangmuyeong.remittance.application.dto.result.RemitResult;
import com.jangmuyeong.remittance.domain.account.Account;
import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.exception.ErrorCode;
import com.jangmuyeong.remittance.domain.ledger.LedgerEntry;
import com.jangmuyeong.remittance.domain.ledger.TransactionType;
import com.jangmuyeong.remittance.domain.limit.DailyLimit;
import com.jangmuyeong.remittance.domain.policy.FeePolicy;
import com.jangmuyeong.remittance.domain.port.AccountPort;
import com.jangmuyeong.remittance.domain.port.DailyLimitPort;
import com.jangmuyeong.remittance.domain.port.LedgerPort;

/**
 * 이체 애플리케이션 서비스
 */
@Service
public class RemittanceService {

	private final AccountPort accountPort;
	private final DailyLimitPort dailyLimitPort;
	private final LedgerPort ledgerPort;
	private final FeePolicy feePolicy;
	private final Clock clock;

	public RemittanceService(AccountPort accountPort, DailyLimitPort dailyLimitPort,
		LedgerPort ledgerPort, FeePolicy feePolicy, Clock clock) {
		this.accountPort = accountPort;
		this.dailyLimitPort = dailyLimitPort;
		this.ledgerPort = ledgerPort;
		this.feePolicy = feePolicy;
		this.clock = clock;
	}

	/**
	 * 이체 실행
	 * 1) 동일 계좌 이체 방지
	 * 2) 데드락 방지 위해 락을 id 오름차순으로 획득
	 * 3) 수수료 계산(1%)
	 * 4) 이체 일 한도 체크(금액 기준)
	 * 5) 출금 계좌: amount + fee 만큼 차감
	 * 6) 수취 계좌: amount 만큼 증가
	 * 7) 원장 기록(TRANSFER_OUT, FEE, TRANSFER_IN)
	 */
	@Transactional
	public RemitResult remit(RemitCommand command) {
		// 1) 동일 계좌 이체 방지
		if (command.fromAccountNo().equals(command.toAccountNo())) {
			throw new DomainException(ErrorCode.SAME_ACCOUNT_TRANSFER_NOT_ALLOWED);
		}

		Long fromId = accountPort.findByAccountNo(command.fromAccountNo())
			.orElseThrow(() -> new DomainException(ErrorCode.ACCOUNT_NOT_FOUND))
			.getId();

		Long toId = accountPort.findByAccountNo(command.toAccountNo())
			.orElseThrow(() -> new DomainException(ErrorCode.ACCOUNT_NOT_FOUND))
			.getId();

		// 2) 데드락 방지: accountId 오름차순으로 락 획득
		Long firstId = Math.min(fromId, toId);
		Long secondId = Math.max(fromId, toId);

		Account first = accountPort.findByIdForUpdate(firstId)
			.orElseThrow(() -> new DomainException(ErrorCode.ACCOUNT_NOT_FOUND));
		Account second = accountPort.findByIdForUpdate(secondId)
			.orElseThrow(() -> new DomainException(ErrorCode.ACCOUNT_NOT_FOUND));

		Account from = fromId.equals(firstId) ? first : second;
		Account to = toId.equals(firstId) ? first : second;

		long fee = feePolicy.calculateFee(command.amount());
		long totalDebit = command.amount() + fee;

		// 요구사항: 이체 일 한도 3,000,000원(이체 금액 기준)
		DailyLimit limit = dailyLimitPort.getOrCreate(from.getId(), LocalDate.now(clock));
		limit.addTransfer(command.amount());
		dailyLimitPort.save(limit);

		// 잔액 변경
		from.withdraw(totalDebit);
		to.deposit(command.amount());

		Account savedFrom = accountPort.save(from);
		Account savedTo = accountPort.save(to);

		Instant now = Instant.now(clock);

		// 송금/수취/수수료를 기록으로 남김
		ledgerPort.save(new LedgerEntry(null, savedFrom.getId(), savedTo.getId(), TransactionType.TRANSFER_OUT,
			command.amount(), 0L, now));
		ledgerPort.save(new LedgerEntry(null, savedFrom.getId(), savedTo.getId(), TransactionType.FEE,
			0L, fee, now));
		ledgerPort.save(new LedgerEntry(null, savedTo.getId(), savedFrom.getId(), TransactionType.TRANSFER_IN,
			command.amount(), 0L, now));

		return new RemitResult(
			savedFrom.getId(),
			savedFrom.getAccountNo(),
			savedTo.getId(),
			savedTo.getAccountNo(),
			command.amount(),
			fee,
			savedFrom.getBalance(),
			savedTo.getBalance()
		);
	}
}
