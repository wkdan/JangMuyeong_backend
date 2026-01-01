package com.jangmuyeong.remittance.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jangmuyeong.remittance.application.dto.command.RemitCommand;
import com.jangmuyeong.remittance.domain.account.Account;
import com.jangmuyeong.remittance.domain.account.AccountStatus;
import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.ledger.LedgerEntry;
import com.jangmuyeong.remittance.domain.ledger.TransactionType;
import com.jangmuyeong.remittance.domain.limit.DailyLimit;
import com.jangmuyeong.remittance.domain.policy.FeePolicy;
import com.jangmuyeong.remittance.domain.port.AccountPort;
import com.jangmuyeong.remittance.domain.port.DailyLimitPort;
import com.jangmuyeong.remittance.domain.port.LedgerPort;

@ExtendWith(MockitoExtension.class)
class RemittanceServiceTest {

	@Mock AccountPort accountPort;
	@Mock DailyLimitPort dailyLimitPort;
	@Mock LedgerPort ledgerPort;
	@Mock FeePolicy feePolicy;

	Clock clock;
	RemittanceService service;

	@BeforeEach
	void setUp() {
		clock = Clock.fixed(Instant.parse("2025-12-30T10:00:00Z"), ZoneOffset.UTC);
		service = new RemittanceService(accountPort, dailyLimitPort, ledgerPort, feePolicy, clock);
	}

	@Test
	void remit_success_applies_fee_and_writes_3_ledgers() {
		long fromId = 1L, toId = 2L;
		String fromNo = "111-222";
		String toNo = "333-444";

		Account baseFrom = new Account(fromId, fromNo, AccountStatus.ACTIVE, 1_000_000L);
		Account baseTo = new Account(toId, toNo, AccountStatus.ACTIVE, 0L);

		Account lockedFrom = new Account(fromId, fromNo, AccountStatus.ACTIVE, 1_000_000L);
		Account lockedTo = new Account(toId, toNo, AccountStatus.ACTIVE, 0L);

		when(accountPort.findByAccountNo(fromNo)).thenReturn(Optional.of(baseFrom));
		when(accountPort.findByAccountNo(toNo)).thenReturn(Optional.of(baseTo));

		when(accountPort.findByIdForUpdate(1L)).thenReturn(Optional.of(lockedFrom));
		when(accountPort.findByIdForUpdate(2L)).thenReturn(Optional.of(lockedTo));

		when(accountPort.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

		when(feePolicy.calculateFee(100_000L)).thenReturn(1_000L);

		DailyLimit limit = new DailyLimit(1L, fromId, LocalDate.now(clock), 0L, 0L);
		when(dailyLimitPort.getOrCreate(eq(fromId), eq(LocalDate.now(clock)))).thenReturn(limit);
		when(dailyLimitPort.save(any(DailyLimit.class))).thenAnswer(inv -> inv.getArgument(0));

		com.jangmuyeong.remittance.application.dto.result.RemitResult res =
			service.remit(new RemitCommand(fromNo, toNo, 100_000L));

		assertThat(res.fee()).isEqualTo(1_000L);
		assertThat(res.fromBalance()).isEqualTo(899_000L); // 1,000,000 - (100,000 + 1,000)
		assertThat(res.toBalance()).isEqualTo(100_000L);
		assertThat(res.fromAccountNo()).isEqualTo(fromNo);
		assertThat(res.toAccountNo()).isEqualTo(toNo);

		ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
		verify(ledgerPort, times(3)).save(captor.capture());
		List<LedgerEntry> entries = captor.getAllValues();

		assertThat(entries).hasSize(3);

		// 1) TRANSFER_OUT
		assertThat(entries.get(0).getType()).isEqualTo(TransactionType.TRANSFER_OUT);
		assertThat(entries.get(0).getAccountId()).isEqualTo(fromId);
		assertThat(entries.get(0).getCounterpartyAccountId()).isEqualTo(toId);
		assertThat(entries.get(0).getAmount()).isEqualTo(100_000L);
		assertThat(entries.get(0).getBalanceAfter()).isEqualTo(899_000L);

		// 2) FEE (수수료 포함 총 차감 결과 잔액과 동일해야 함)
		assertThat(entries.get(1).getType()).isEqualTo(TransactionType.FEE);
		assertThat(entries.get(1).getAccountId()).isEqualTo(fromId);
		assertThat(entries.get(1).getFeeAmount()).isEqualTo(1_000L);
		assertThat(entries.get(1).getAmount()).isEqualTo(0L);
		assertThat(entries.get(1).getBalanceAfter()).isEqualTo(899_000L);

		// 3) TRANSFER_IN
		assertThat(entries.get(2).getType()).isEqualTo(TransactionType.TRANSFER_IN);
		assertThat(entries.get(2).getAccountId()).isEqualTo(toId);
		assertThat(entries.get(2).getCounterpartyAccountId()).isEqualTo(fromId);
		assertThat(entries.get(2).getAmount()).isEqualTo(100_000L);
		assertThat(entries.get(2).getBalanceAfter()).isEqualTo(100_000L);
	}

	@Test
	void remit_throws_when_same_account() {
		assertThatThrownBy(() -> service.remit(new RemitCommand("111-222", "111-222", 1000L)))
			.isInstanceOf(DomainException.class);

		verifyNoInteractions(accountPort, dailyLimitPort, ledgerPort, feePolicy);
	}

	@Test
	void remit_locks_in_ascending_order_to_avoid_deadlock() {
		// 일부러 from > to (id 기준 데드락 방지 락 순서 검증)
		long fromId = 5L, toId = 3L;
		String fromNo = "555-666";
		String toNo = "333-444";

		Account baseFrom = new Account(fromId, fromNo, AccountStatus.ACTIVE, 1_000_000L);
		Account baseTo = new Account(toId, toNo, AccountStatus.ACTIVE, 0L);

		Account locked3 = new Account(3L, toNo, AccountStatus.ACTIVE, 0L);
		Account locked5 = new Account(5L, fromNo, AccountStatus.ACTIVE, 1_000_000L);

		when(accountPort.findByAccountNo(fromNo)).thenReturn(Optional.of(baseFrom));
		when(accountPort.findByAccountNo(toNo)).thenReturn(Optional.of(baseTo));

		when(accountPort.findByIdForUpdate(3L)).thenReturn(Optional.of(locked3));
		when(accountPort.findByIdForUpdate(5L)).thenReturn(Optional.of(locked5));
		when(accountPort.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

		when(feePolicy.calculateFee(100_000L)).thenReturn(1_000L);
		DailyLimit limit = new DailyLimit(1L, 5L, LocalDate.now(clock), 0L, 0L);
		when(dailyLimitPort.getOrCreate(eq(5L), eq(LocalDate.now(clock)))).thenReturn(limit);
		when(dailyLimitPort.save(any(DailyLimit.class))).thenAnswer(inv -> inv.getArgument(0));

		service.remit(new RemitCommand(fromNo, toNo, 100_000L));

		InOrder inOrder = inOrder(accountPort);
		inOrder.verify(accountPort).findByIdForUpdate(3L);
		inOrder.verify(accountPort).findByIdForUpdate(5L);
	}

	@Test
	void remit_throws_when_transfer_daily_limit_exceeded_and_does_not_save_accounts_or_ledgers() {
		long fromId = 1L, toId = 2L;
		String fromNo = "111-222";
		String toNo = "333-444";

		Account baseFrom = new Account(fromId, fromNo, AccountStatus.ACTIVE, 10_000_000L);
		Account baseTo = new Account(toId, toNo, AccountStatus.ACTIVE, 0L);

		Account lockedFrom = new Account(fromId, fromNo, AccountStatus.ACTIVE, 10_000_000L);
		Account lockedTo = new Account(toId, toNo, AccountStatus.ACTIVE, 0L);

		when(accountPort.findByAccountNo(fromNo)).thenReturn(Optional.of(baseFrom));
		when(accountPort.findByAccountNo(toNo)).thenReturn(Optional.of(baseTo));

		when(accountPort.findByIdForUpdate(1L)).thenReturn(Optional.of(lockedFrom));
		when(accountPort.findByIdForUpdate(2L)).thenReturn(Optional.of(lockedTo));
		when(feePolicy.calculateFee(anyLong())).thenReturn(0L);

		// 이미 2,000,000 이체된 상태에서 1,500,000 추가 -> 한도 초과
		DailyLimit limit = new DailyLimit(1L, fromId, LocalDate.now(clock), 0L, 2_000_000L);
		when(dailyLimitPort.getOrCreate(eq(fromId), eq(LocalDate.now(clock)))).thenReturn(limit);

		assertThatThrownBy(() -> service.remit(new RemitCommand(fromNo, toNo, 1_500_000L)))
			.isInstanceOf(DomainException.class);

		verify(dailyLimitPort, never()).save(any()); // addTransfer에서 터짐
		verify(accountPort, never()).save(any());
		verify(ledgerPort, never()).save(any());
	}
}
