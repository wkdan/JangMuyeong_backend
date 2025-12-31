package com.jangmuyeong.remittance.application.dto.command;

/**
 * 이체 커맨드
 */
public record RemitCommand(Long fromAccountId, Long toAccountId, long amount) {}