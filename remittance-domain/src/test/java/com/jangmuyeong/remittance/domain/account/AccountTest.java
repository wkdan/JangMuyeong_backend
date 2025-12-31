package com.jangmuyeong.remittance.domain.account;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.exception.ErrorCode;

class AccountTest {

	@Test
	void delete_marks_account_deleted() {
		Account a = new Account(1L, "111-222", AccountStatus.ACTIVE, 0);

		a.delete();

		assertThat(a.getStatus()).isEqualTo(AccountStatus.DELETED);
	}

	@Test
	void deposit_increases_balance() {
		Account a = new Account(1L, "111-222", AccountStatus.ACTIVE, 0);

		a.deposit(1_000_000);

		assertThat(a.getBalance()).isEqualTo(1_000_000);
	}

	@Test
	void withdraw_decreases_balance() {
		Account a = new Account(1L, "111-222", AccountStatus.ACTIVE, 1_000_000);

		a.withdraw(200_000);

		assertThat(a.getBalance()).isEqualTo(800_000);
	}

	@Test
	void deposit_rejects_non_positive_amount() {
		Account a = new Account(1L, "111-222", AccountStatus.ACTIVE, 0);

		assertThatThrownBy(() -> a.deposit(0))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining(ErrorCode.INVALID_AMOUNT.name());

		assertThatThrownBy(() -> a.deposit(-1))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining(ErrorCode.INVALID_AMOUNT.name());
	}

	@Test
	void withdraw_rejects_non_positive_amount() {
		Account a = new Account(1L, "111-222", AccountStatus.ACTIVE, 1000);

		assertThatThrownBy(() -> a.withdraw(0))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining(ErrorCode.INVALID_AMOUNT.name());

		assertThatThrownBy(() -> a.withdraw(-1))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining(ErrorCode.INVALID_AMOUNT.name());
	}

	@Test
	void withdraw_rejects_insufficient_balance() {
		Account a = new Account(1L, "111-222", AccountStatus.ACTIVE, 1000);

		assertThatThrownBy(() -> a.withdraw(2000))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining(ErrorCode.INSUFFICIENT_BALANCE.name());
	}

	@Test
	void deleted_account_rejects_deposit_and_withdraw() {
		Account a = new Account(1L, "111-222", AccountStatus.DELETED, 0);

		assertThatThrownBy(() -> a.deposit(1000))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining(ErrorCode.ACCOUNT_INACTIVE.name());

		assertThatThrownBy(() -> a.withdraw(1000))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining(ErrorCode.ACCOUNT_INACTIVE.name());
	}
}
