package com.jangmuyeong.remittance.application.dto.result;

/**
 * 잔액 변경(입/출금) 결과
 */
public record BalanceResult(Long accountId, long balance) {}