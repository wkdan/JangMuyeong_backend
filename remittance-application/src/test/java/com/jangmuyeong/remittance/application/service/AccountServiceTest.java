package com.jangmuyeong.remittance.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jangmuyeong.remittance.application.dto.command.CreateAccountCommand;
import com.jangmuyeong.remittance.domain.account.Account;
import com.jangmuyeong.remittance.domain.account.AccountStatus;
import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.port.AccountPort;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock AccountPort accountPort;
	@InjectMocks AccountService service;

	@Test
	void create_success_when_accountNo_not_exists() {
		when(accountPort.findByAccountNo("111-222")).thenReturn(Optional.empty());
		when(accountPort.save(any(Account.class)))
			.thenReturn(new Account(1L, "111-222", AccountStatus.ACTIVE, 0L));

		com.jangmuyeong.remittance.application.dto.result.CreateAccountResult res =
			service.create(new CreateAccountCommand("111-222"));

		assertThat(res.accountId()).isEqualTo(1L);
		assertThat(res.accountNo()).isEqualTo("111-222");
		verify(accountPort).save(any(Account.class));
	}

	@Test
	void create_throws_when_duplicate_accountNo() {
		when(accountPort.findByAccountNo("111-222"))
			.thenReturn(Optional.of(new Account(1L, "111-222", AccountStatus.ACTIVE, 0L)));

		assertThatThrownBy(() -> service.create(new CreateAccountCommand("111-222")))
			.isInstanceOf(DomainException.class);

		verify(accountPort, never()).save(any());
	}

	@Test
	void delete_marks_deleted_and_saves() {
		Account base = new Account(1L, "111-222", AccountStatus.ACTIVE, 0L);
		Account locked = new Account(1L, "111-222", AccountStatus.ACTIVE, 0L);

		when(accountPort.findByAccountNo("111-222")).thenReturn(Optional.of(base));
		when(accountPort.findByIdForUpdate(1L)).thenReturn(Optional.of(locked));
		when(accountPort.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

		service.delete("111-222");

		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
		verify(accountPort).save(captor.capture());
		assertThat(captor.getValue().getStatus()).isEqualTo(AccountStatus.DELETED);
	}

	@Test
	void delete_throws_when_account_not_found() {
		when(accountPort.findByAccountNo("999-000")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete("999-000"))
			.isInstanceOf(DomainException.class);

		verify(accountPort, never()).save(any());
	}
}
