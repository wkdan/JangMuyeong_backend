package com.jangmuyeong.remittance.presentation.response;

import java.time.Instant;

/**
 * 거래내역 조회 API 응답 아이템
 *
 * - domain(LedgerEntry)을 그대로 노출하지 않고, API 스펙에 맞는 필드만 전달한다.
 * - type은 enum 값을 문자열로 내려 프론트/클라이언트가 단순 처리하도록 한다.
 *
 * - id: 원장(거래내역) 식별자
 * - type: 거래 타입(DEPOSIT/WITHDRAW/TRANSFER_OUT/TRANSFER_IN/FEE)
 * - counterpartyAccountId: 상대 계좌 ID(입금/출금은 null)
 * - amount: 거래 금액(수수료(FEE) 타입은 0으로 내려갈 수 있음)
 * - feeAmount: 수수료 금액(일반 거래는 0, FEE 타입에서만 값이 존재)
 * - occurredAt: 발생 시각(UTC Instant)
 */
public record TransactionItemResponse(
	Long id,
	String type,
	Long counterpartyAccountId,
	long amount,
	long feeAmount,
	Instant occurredAt
) {}
