package com.jangmuyeong.remittance.application.dto.command;

/**
 * 계좌 생성 커맨드
 * 필요한 값만 전달
 */
public record CreateAccountCommand(String accountNo) {}