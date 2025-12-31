package com.jangmuyeong.remittance.presentation.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jangmuyeong.remittance.application.dto.command.CreateAccountCommand;
import com.jangmuyeong.remittance.application.dto.command.DepositCommand;
import com.jangmuyeong.remittance.application.dto.command.WithdrawCommand;
import com.jangmuyeong.remittance.application.dto.result.BalanceResult;
import com.jangmuyeong.remittance.application.dto.result.CreateAccountResult;
import com.jangmuyeong.remittance.application.service.AccountService;
import com.jangmuyeong.remittance.application.service.MoneyService;
import com.jangmuyeong.remittance.presentation.request.AmountRequest;
import com.jangmuyeong.remittance.presentation.request.CreateAccountRequest;
import com.jangmuyeong.remittance.presentation.response.ApiResponse;

import jakarta.validation.Valid;

/**
 * 계좌 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

	private final AccountService accountService;
	private final MoneyService moneyService;

	public AccountController(AccountService accountService, MoneyService moneyService) {
		this.accountService = accountService;
		this.moneyService = moneyService;
	}

	/**
	 * 계좌 등록 API
	 * 새로운 계좌를 등록
	 */
	@PostMapping
	public ApiResponse<CreateAccountResult> create(@Valid @RequestBody CreateAccountRequest req) {
		return ApiResponse.of(accountService.create(new CreateAccountCommand(req.accountNo())));
	}

	/**
	 * 계좌 삭제 API
	 * 기존 계좌를 삭제
	 * 구현: 소프트 삭제(상태를 DELETED로 변경)로 처리
	 */
	@DeleteMapping("/{accountId}")
	public ApiResponse<Void> delete(@PathVariable("accountId") Long accountId) {
		accountService.delete(accountId);
		return ApiResponse.of(null);
	}

	/**
	 * 입금 API
	 * 특정 계좌에 금액을 입금
	 */
	@PostMapping("/{accountId}/deposit")
	public ApiResponse<BalanceResult> deposit(
		@PathVariable("accountId") Long accountId,
		@Valid @RequestBody AmountRequest req
	) {
		return ApiResponse.of(moneyService.deposit(new DepositCommand(accountId, req.amount())));
	}

	/**
	 * 출금 API
	 * 특정 계좌에서 금액을 출금
	 * 출금 일 한도(1,000,000원)를 체크
	 */
	@PostMapping("/{accountId}/withdraw")
	public ApiResponse<BalanceResult> withdraw(
		@PathVariable("accountId") Long accountId,
		@Valid @RequestBody AmountRequest req
	) {
		return ApiResponse.of(moneyService.withdraw(new WithdrawCommand(accountId, req.amount())));
	}
}
