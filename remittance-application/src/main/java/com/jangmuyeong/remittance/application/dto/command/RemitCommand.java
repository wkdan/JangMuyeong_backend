package com.jangmuyeong.remittance.application.dto.command;

/**
 * 이체 커맨드
 */
public record RemitCommand(String fromAccountNo, String toAccountNo, long amount) {
}