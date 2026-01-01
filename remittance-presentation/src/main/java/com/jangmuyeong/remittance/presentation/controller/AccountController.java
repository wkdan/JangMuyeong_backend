package com.jangmuyeong.remittance.presentation.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.jangmuyeong.remittance.presentation.response.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 계좌 관련 REST API 컨트롤러
 */
@Tag(name = "Accounts", description = "계좌 생성/삭제 및 입금/출금 API")
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
	@Operation(summary = "계좌 등록")
	@PostMapping
	public RsData<CreateAccountResult> create(@Valid @RequestBody CreateAccountRequest req) {
		return RsData.of(accountService.create(new CreateAccountCommand(req.accountNo())));
	}

	/**
	 * 계좌 삭제 API
	 * 기존 계좌를 삭제
	 * 구현: 소프트 삭제(상태를 DELETED로 변경)로 처리
	 */
	@Operation(summary = "계좌 삭제")
	@DeleteMapping("/{accountNo}")
	public RsData<Void> delete(@PathVariable("accountNo") String accountNo) {
		accountService.delete(accountNo);
		return RsData.of(null);
	}

	/**
	 * 입금 API
	 * 특정 계좌에 금액을 입금
	 */
	@Operation(summary = "입금")
	@PostMapping("/{accountNo}/deposit")
	public RsData<BalanceResult> deposit(
		@PathVariable("accountNo") String accountNo,
		@Valid @RequestBody AmountRequest req
	) {
		return RsData.of(moneyService.deposit(new DepositCommand(accountNo, req.amount())));
	}

	/**
	 * 출금 API
	 * 특정 계좌에서 금액을 출금
	 * 출금 일 한도(1,000,000원)를 체크
	 */
	@Operation(summary = "출금")
	@PostMapping("/{accountNo}/withdraw")
	public RsData<BalanceResult> withdraw(
		@PathVariable("accountNo") String accountNo,
		@Valid @RequestBody AmountRequest req
	) {
		return RsData.of(moneyService.withdraw(new WithdrawCommand(accountNo, req.amount())));
	}

	/**
	 * 잔액 조회 API
	 * 특정 계좌의 현재 잔액을 조회
	 */
	@Operation(summary = "잔액 조회")
	@GetMapping("/{accountNo}/balance")
	public RsData<BalanceResult> balance(@PathVariable("accountNo") String accountNo) {
		return RsData.of(moneyService.balance(accountNo));
	}
}
