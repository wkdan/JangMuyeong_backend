package com.jangmuyeong.remittance.domain.ledger;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class LedgerEntryTest {

	@Test
	void ledger_entry_holds_values() {
		Instant now = Instant.now();

		LedgerEntry e = new LedgerEntry(
			10L,
			1L,
			2L,
			TransactionType.TRANSFER_OUT,
			100_000,
			0,
			now,
			899_000
		);

		assertThat(e.getId()).isEqualTo(10L);
		assertThat(e.getAccountId()).isEqualTo(1L);
		assertThat(e.getCounterpartyAccountId()).isEqualTo(2L);
		assertThat(e.getType()).isEqualTo(TransactionType.TRANSFER_OUT);
		assertThat(e.getAmount()).isEqualTo(100_000);
		assertThat(e.getFeeAmount()).isEqualTo(0);
		assertThat(e.getOccurredAt()).isEqualTo(now);
		assertThat(e.getBalanceAfter()).isEqualTo(899_000);
	}
}
