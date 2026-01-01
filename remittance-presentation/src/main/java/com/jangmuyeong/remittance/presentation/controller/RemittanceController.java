package com.jangmuyeong.remittance.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jangmuyeong.remittance.application.dto.command.RemitCommand;
import com.jangmuyeong.remittance.application.dto.result.RemitResult;
import com.jangmuyeong.remittance.application.service.RemittanceService;
import com.jangmuyeong.remittance.presentation.request.RemitRequest;
import com.jangmuyeong.remittance.presentation.response.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 이체(송금) API 컨트롤러
 */
@Tag(name = "Remittances", description = "이체(송금) API")
@RestController
@RequestMapping("/remittances")
public class RemittanceController {

	private final RemittanceService service;

	public RemittanceController(RemittanceService service) {
		this.service = service;
	}

	/**
	 * 이체 API
	 */
	@Operation(summary = "이체")
	@PostMapping
	public RsData<RemitResult> remit(@Valid @RequestBody RemitRequest req) {
		return RsData.of(service.remit(
			new RemitCommand(req.fromAccountNo(), req.toAccountNo(), req.amount())
		));
	}
}