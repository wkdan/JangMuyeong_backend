package com.jangmuyeong.remittance.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.jangmuyeong.remittance.domain.account.Account;
import com.jangmuyeong.remittance.domain.account.AccountStatus;
import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.ledger.LedgerEntry;
import com.jangmuyeong.remittance.domain.ledger.TransactionType;
import com.jangmuyeong.remittance.domain.port.AccountPort;
import com.jangmuyeong.remittance.domain.port.LedgerPort;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TransactionQueryServiceTest {

	@Mock AccountPort accountPort;
	@Mock LedgerPort ledgerPort;

	@InjectMocks TransactionQueryService service;

	@Test
	void latest_returns_ledger_list_when_account_exists() {
		Account a = new Account(1L, "111-222", AccountStatus.ACTIVE, 0L);
		when(accountPort.findByAccountNo("111-222")).thenReturn(Optional.of(a));

		List<LedgerEntry> ledgers = List.of(
			new LedgerEntry(
				1L,
				1L,
				null,
				TransactionType.DEPOSIT,
				1000L,
				0L,
				Instant.parse("2025-12-30T10:00:00Z"),
				1000L
			)
		);
		when(ledgerPort.findLatestByAccountId(1L, 20)).thenReturn(ledgers);

		List<LedgerEntry> res = service.latest("111-222", 20);

		assertThat(res).hasSize(1);
		verify(ledgerPort).findLatestByAccountId(1L, 20);
	}

	@Test
	void latest_throws_when_account_not_found() {
		when(accountPort.findByAccountNo("999-000")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.latest("999-000", 20))
			.isInstanceOf(DomainException.class);

		verify(ledgerPort, never()).findLatestByAccountId(anyLong(), anyInt());
	}
}