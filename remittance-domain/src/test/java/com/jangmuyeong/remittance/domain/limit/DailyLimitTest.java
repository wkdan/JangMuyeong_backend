package com.jangmuyeong.remittance.domain.limit;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.exception.ErrorCode;

class DailyLimitTest {

	@Test
	void withdraw_limit_allows_exactly_1_000_000() {
		DailyLimit limit = new DailyLimit(1L, 1L, LocalDate.now(), 0, 0);

		limit.addWithdraw(400_000);
		limit.addWithdraw(600_000);

		assertThat(limit.getWithdrawSum()).isEqualTo(1_000_000);
	}

	@Test
	void withdraw_limit_rejects_over_1_000_000() {
		DailyLimit limit = new DailyLimit(1L, 1L, LocalDate.now(), 0, 0);

		limit.addWithdraw(900_000);

		assertThatThrownBy(() -> limit.addWithdraw(200_000))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining(ErrorCode.WITHDRAW_DAILY_LIMIT_EXCEEDED.name());
	}

	@Test
	void transfer_limit_allows_exactly_3_000_000() {
		DailyLimit limit = new DailyLimit(1L, 1L, LocalDate.now(), 0, 0);

		limit.addTransfer(2_000_000);
		limit.addTransfer(1_000_000);

		assertThat(limit.getTransferSum()).isEqualTo(3_000_000);
	}

	@Test
	void transfer_limit_rejects_over_3_000_000() {
		DailyLimit limit = new DailyLimit(1L, 1L, LocalDate.now(), 0, 0);

		limit.addTransfer(2_000_000);

		assertThatThrownBy(() -> limit.addTransfer(1_500_000))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining(ErrorCode.TRANSFER_DAILY_LIMIT_EXCEEDED.name());
	}
}