package com.jangmuyeong.remittance.domain.limit;

import java.time.LocalDate;

import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.exception.ErrorCode;

/**
 * 1일 한도(출금/이체) 누적치 관리 도메인
 * - 출금: 1,000,000원/일
 * - 이체: 3,000,000원/일 (이체 금액 기준)
 */
public class DailyLimit {

	private static final long WITHDRAW_DAILY_LIMIT = 1_000_000L;
	private static final long TRANSFER_DAILY_LIMIT = 3_000_000L;

	private final Long id;
	private final Long accountId;
	private final LocalDate date;
	private long withdrawSum;
	private long transferSum;

	public DailyLimit(Long id, Long accountId, LocalDate date, long withdrawSum, long transferSum) {
		this.id = id;
		this.accountId = accountId;
		this.date = date;
		this.withdrawSum = withdrawSum;
		this.transferSum = transferSum;
	}

	public Long getId() { return id; }
	public Long getAccountId() { return accountId; }
	public LocalDate getDate() { return date; }
	public long getWithdrawSum() { return withdrawSum; }
	public long getTransferSum() { return transferSum; }

	/** 출금 누적액을 증가시키며, 일 한도를 초과하면 예외 */
	public void addWithdraw(long amount) {
		long next = withdrawSum + amount;
		if (next > WITHDRAW_DAILY_LIMIT) throw new DomainException(ErrorCode.WITHDRAW_DAILY_LIMIT_EXCEEDED);
		this.withdrawSum = next;
	}

	/** 이체 누적액을 증가시키며, 일 한도를 초과하면 예외 */
	public void addTransfer(long amount) {
		long next = transferSum + amount;
		if (next > TRANSFER_DAILY_LIMIT) throw new DomainException(ErrorCode.TRANSFER_DAILY_LIMIT_EXCEEDED);
		this.transferSum = next;
	}
}
