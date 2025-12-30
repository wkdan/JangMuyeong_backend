package com.jangmuyeong.remittance.domain.account;

import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.exception.ErrorCode;

/**
 * 계좌 도메인
 */
public class Account {

	private final Long id;
	private final String accountNo;
	private AccountStatus status;
	private long balance;

	public Account(Long id, String accountNo, AccountStatus status, long balance) {
		this.id = id;
		this.accountNo = accountNo;
		this.status = status;
		this.balance = balance;
	}

	public Long getId() { return id; }
	public String getAccountNo() { return accountNo; }
	public AccountStatus getStatus() { return status; }
	public long getBalance() { return balance; }

	/**
	 * 계좌 삭제(비활성화) 처리, 실제 삭제 대신 소프트 딜리트로 운용
	 */
	public void delete() {
		this.status = AccountStatus.DELETED;
	}

	/**
	 * 입금: 활성 계좌 + 0원 초과일 시 허용
	 */
	public void deposit(long amount) {
		validateActive();
		validatePositive(amount);
		this.balance += amount;
	}

	/**
	 * 출금: 활성 계좌, 0원 초과, 잔액 부족 불가
	 */
	public void withdraw(long amount) {
		validateActive();
		validatePositive(amount);
		if (this.balance < amount) throw new DomainException(ErrorCode.INSUFFICIENT_BALANCE);
		this.balance -= amount;
	}

	/**
	 * 활성 계좌인지 검증
	 */
	private void validateActive() {
		if (status != AccountStatus.ACTIVE) throw new DomainException(ErrorCode.ACCOUNT_INACTIVE);
	}

	/**
	 * 금액이 양수인지 검증
	 */
	private void validatePositive(long amount) {
		if (amount <= 0) throw new DomainException(ErrorCode.INVALID_AMOUNT);
	}
}