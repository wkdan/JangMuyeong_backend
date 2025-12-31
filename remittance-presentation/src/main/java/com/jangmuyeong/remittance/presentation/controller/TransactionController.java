package com.jangmuyeong.remittance.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jangmuyeong.remittance.application.service.TransactionQueryService;
import com.jangmuyeong.remittance.presentation.response.ApiResponse;
import com.jangmuyeong.remittance.presentation.response.TransactionItemResponse;

/**
 * 거래내역 조회 API 컨트롤러
 */
@RestController
@RequestMapping("/accounts/{accountId}/transactions")
public class TransactionController {

	private final TransactionQueryService queryService;

	public TransactionController(TransactionQueryService queryService) {
		this.queryService = queryService;
	}

	/**
	 * 거래내역 최신순 조회 API
	 *
	 * @param accountId 대상 계좌 ID
	 * @param size      반환 개수(기본 20)
	 */
	@GetMapping
	public ApiResponse<List<TransactionItemResponse>> latest(
		@PathVariable("accountId") Long accountId,
		@RequestParam(defaultValue = "20") int size
	) {
		// 요구사항: 최신순(정렬은 queryService/infra에서 보장)
		var list = queryService.latest(accountId, size).stream()
			.map(e -> new TransactionItemResponse(
				e.getId(),
				e.getType().name(),
				e.getCounterpartyAccountId(),
				e.getAmount(),
				e.getFeeAmount(),
				e.getOccurredAt()
			))
			.toList();

		return ApiResponse.of(list);
	}
}
