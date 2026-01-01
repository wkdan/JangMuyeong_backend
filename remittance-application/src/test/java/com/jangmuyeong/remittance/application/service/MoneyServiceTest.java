package com.jangmuyeong.remittance.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.jangmuyeong.remittance.application.dto.command.DepositCommand;
import com.jangmuyeong.remittance.application.dto.command.WithdrawCommand;
import com.jangmuyeong.remittance.domain.account.Account;
import com.jangmuyeong.remittance.domain.account.AccountStatus;
import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.ledger.LedgerEntry;
import com.jangmuyeong.remittance.domain.ledger.TransactionType;
import com.jangmuyeong.remittance.domain.limit.DailyLimit;
import com.jangmuyeong.remittance.domain.port.AccountPort;
import com.jangmuyeong.remittance.domain.port.DailyLimitPort;
import com.jangmuyeong.remittance.domain.port.LedgerPort;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MoneyServiceTest {

	@Mock AccountPort accountPort;
	@Mock DailyLimitPort dailyLimitPort;
	@Mock LedgerPort ledgerPort;

	Clock clock;
	MoneyService service;

	@BeforeEach
	void setUp() {
		clock = Clock.fixed(Instant.parse("2025-12-30T10:00:00Z"), ZoneOffset.UTC);
		service = new MoneyService(accountPort, dailyLimitPort, ledgerPort, clock);
	}

	@Test
	void deposit_success_saves_account_and_ledger() {
		Account base = new Account(1L, "111-222", AccountStatus.ACTIVE, 0L);
		Account locked = new Account(1L, "111-222", AccountStatus.ACTIVE, 0L);

		when(accountPort.findByAccountNo("111-222")).thenReturn(Optional.of(base));
		when(accountPort.findByIdForUpdate(1L)).thenReturn(Optional.of(locked));
		when(accountPort.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

		com.jangmuyeong.remittance.application.dto.result.BalanceResult res =
			service.deposit(new DepositCommand("111-222", 1_000_000));

		assertThat(res.accountId()).isEqualTo(1L);
		assertThat(res.accountNo()).isEqualTo("111-222");
		assertThat(res.balance()).isEqualTo(1_000_000);

		ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
		verify(ledgerPort).save(captor.capture());
		LedgerEntry e = captor.getValue();

		assertThat(e.getAccountId()).isEqualTo(1L);
		assertThat(e.getType()).isEqualTo(TransactionType.DEPOSIT);
		assertThat(e.getAmount()).isEqualTo(1_000_000);
		assertThat(e.getFeeAmount()).isEqualTo(0);
		assertThat(e.getOccurredAt()).isEqualTo(Instant.now(clock));
		assertThat(e.getBalanceAfter()).isEqualTo(1_000_000);
	}

	@Test
	void deposit_throws_when_account_not_found() {
		when(accountPort.findByAccountNo("999-000")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.deposit(new DepositCommand("999-000", 1000)))
			.isInstanceOf(DomainException.class);

		verify(accountPort, never()).save(any());
		verify(ledgerPort, never()).save(any());
	}

	@Test
	void withdraw_success_checks_limit_and_saves_ledger() {
		Account base = new Account(1L, "111-222", AccountStatus.ACTIVE, 1_000_000L);
		Account locked = new Account(1L, "111-222", AccountStatus.ACTIVE, 1_000_000L);

		when(accountPort.findByAccountNo("111-222")).thenReturn(Optional.of(base));
		when(accountPort.findByIdForUpdate(1L)).thenReturn(Optional.of(locked));
		when(accountPort.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

		DailyLimit limit = new DailyLimit(1L, 1L, LocalDate.now(clock), 0L, 0L);
		when(dailyLimitPort.getOrCreate(eq(1L), eq(LocalDate.now(clock)))).thenReturn(limit);
		when(dailyLimitPort.save(any(DailyLimit.class))).thenAnswer(inv -> inv.getArgument(0));

		com.jangmuyeong.remittance.application.dto.result.BalanceResult res =
			service.withdraw(new WithdrawCommand("111-222", 200_000));

		assertThat(res.accountNo()).isEqualTo("111-222");
		assertThat(res.balance()).isEqualTo(800_000);

		ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
		verify(ledgerPort).save(captor.capture());
		LedgerEntry e = captor.getValue();

		assertThat(e.getType()).isEqualTo(TransactionType.WITHDRAW);
		assertThat(e.getAmount()).isEqualTo(200_000);
		assertThat(e.getBalanceAfter()).isEqualTo(800_000);
	}

	@Test
	void withdraw_throws_when_daily_limit_exceeded_and_does_not_save_account_or_ledger() {
		Account base = new Account(1L, "111-222", AccountStatus.ACTIVE, 1_000_000L);
		Account locked = new Account(1L, "111-222", AccountStatus.ACTIVE, 1_000_000L);

		when(accountPort.findByAccountNo("111-222")).thenReturn(Optional.of(base));
		when(accountPort.findByIdForUpdate(1L)).thenReturn(Optional.of(locked));

		// 이미 900,000 출금된 상태에서 200,000 추가 시 한도 초과
		DailyLimit limit = new DailyLimit(1L, 1L, LocalDate.now(clock), 900_000L, 0L);
		when(dailyLimitPort.getOrCreate(eq(1L), eq(LocalDate.now(clock)))).thenReturn(limit);

		assertThatThrownBy(() -> service.withdraw(new WithdrawCommand("111-222", 200_000)))
			.isInstanceOf(DomainException.class);

		verify(dailyLimitPort, never()).save(any());   // addWithdraw에서 터져서 save까지 못감
		verify(accountPort, never()).save(any());
		verify(ledgerPort, never()).save(any());
	}
}
