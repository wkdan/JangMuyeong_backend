package com.jangmuyeong.remittance.application.dto.command;

/**
 * 출금 커맨드
 */
public record WithdrawCommand(Long accountId, long amount) {}