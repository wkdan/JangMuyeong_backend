package com.jangmuyeong.remittance.domain.ledger;

import java.time.Instant;

/**
 * 거래 내역 엔트리
 * 어떤 거래가 언제 발생했는지를 기록
 * - amount: 실제 이체 or 입출금 금액
 * - feeAmount: 수수로 금액
 */
public class LedgerEntry {

	private final Long id;
	private final Long accountId;
	private final Long counterpartyAccountId;
	private final TransactionType type;
	private final long amount;
	private final long feeAmount;
	private final Instant occurredAt;

	public LedgerEntry(Long id, Long accountId, Long counterpartyAccountId,
		TransactionType type, long amount, long feeAmount, Instant occurredAt) {
		this.id = id;
		this.accountId = accountId;
		this.counterpartyAccountId = counterpartyAccountId;
		this.type = type;
		this.amount = amount;
		this.feeAmount = feeAmount;
		this.occurredAt = occurredAt;
	}

	public Long getId() { return id; }
	public Long getAccountId() { return accountId; }
	public Long getCounterpartyAccountId() { return counterpartyAccountId; }
	public TransactionType getType() { return type; }
	public long getAmount() { return amount; }
	public long getFeeAmount() { return feeAmount; }
	public Instant getOccurredAt() { return occurredAt; }
}