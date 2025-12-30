package com.jangmuyeong.remittance.domain.port;

import java.util.Optional;

import com.jangmuyeong.remittance.domain.account.Account;

/**
 * Account 저장소 포트
 */
public interface AccountPort {
	Optional<Account> findById(Long accountId);

	/** 정합성 보장을 위한 락 조회(출금/이체 등 잔액 변경 시 사용) */
	Optional<Account> findByIdForUpdate(Long accountId);

	Optional<Account> findByAccountNo(String accountNo);

	Account save(Account account);
}
