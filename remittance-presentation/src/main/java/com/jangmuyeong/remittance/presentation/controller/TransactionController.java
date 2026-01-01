package com.jangmuyeong.remittance.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jangmuyeong.remittance.application.service.TransactionQueryService;
import com.jangmuyeong.remittance.presentation.response.RsData;
import com.jangmuyeong.remittance.presentation.response.TransactionItemResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 거래내역 조회 API 컨트롤러
 */
@Tag(name = "Transactions", description = "거래내역 조회 API")
@RestController
@RequestMapping("/accounts/{accountNo}/transactions")
public class TransactionController {

	private final TransactionQueryService queryService;

	public TransactionController(TransactionQueryService queryService) {
		this.queryService = queryService;
	}

	/**
	 * 거래내역 최신순 조회 API
	 *
	 * @param accountNo 대상 계좌
	 * @param size      반환 개수(기본 20)
	 */
	@Operation(summary = "거래내역 최신순 조회")
	@GetMapping
	public RsData<List<TransactionItemResponse>> latest(
		@PathVariable("accountNo") String accountNo,
		@RequestParam(defaultValue = "20") int size
	) {
		List<TransactionItemResponse> list = queryService.latest(accountNo, size).stream()
			.map(e -> new TransactionItemResponse(
				e.getId(),
				e.getType().name(),
				e.getCounterpartyAccountId(),
				e.getAmount(),
				e.getFeeAmount(),
				e.getOccurredAt()
			))
			.toList();

		return RsData.of(list);
	}
}
