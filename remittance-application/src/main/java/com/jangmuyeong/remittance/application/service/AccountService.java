package com.jangmuyeong.remittance.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jangmuyeong.remittance.application.dto.command.CreateAccountCommand;
import com.jangmuyeong.remittance.application.dto.result.CreateAccountResult;
import com.jangmuyeong.remittance.domain.account.Account;
import com.jangmuyeong.remittance.domain.account.AccountStatus;
import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.exception.ErrorCode;
import com.jangmuyeong.remittance.domain.port.AccountPort;

/**
 * 계좌 관련 애플리케이션 서비스
 */
@Service
public class AccountService {

	private final AccountPort accountPort;

	public AccountService(AccountPort accountPort) {
		this.accountPort = accountPort;
	}

	/**
	 * 계좌 생성
	 */
	@Transactional
	public CreateAccountResult create(CreateAccountCommand command) {
		// 중복 계좌번호 방지
		accountPort.findByAccountNo(command.accountNo())
			.ifPresent(a -> { throw new DomainException(ErrorCode.DUPLICATE_ACCOUNT_NO); });

		Account saved = accountPort.save(new Account(null, command.accountNo(), AccountStatus.ACTIVE, 0L));
		return new CreateAccountResult(saved.getId(), saved.getAccountNo());
	}

	/**
	 * 계좌 삭제(비활성화)
	 * 상태를 DELETE로 변경
	 */
	@Transactional
	public void delete(Long accountId) {
		//for update 락으로 조회
		Account account = accountPort.findByIdForUpdate(accountId)
			.orElseThrow(() -> new DomainException(ErrorCode.ACCOUNT_NOT_FOUND));

		account.delete();
		accountPort.save(account);
	}
}
