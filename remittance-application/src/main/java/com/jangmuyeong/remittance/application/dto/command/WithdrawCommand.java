package com.jangmuyeong.remittance.application.dto.command;

/**
 * 출금 커맨드
 */
public record WithdrawCommand(String accountNo, long amount) {
}