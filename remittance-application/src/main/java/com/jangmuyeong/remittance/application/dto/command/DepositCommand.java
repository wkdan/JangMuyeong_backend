package com.jangmuyeong.remittance.application.dto.command;

/**
 * 입금 커맨드
 */
public record DepositCommand(Long accountId, long amount) {}