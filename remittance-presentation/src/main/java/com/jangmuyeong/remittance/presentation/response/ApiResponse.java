package com.jangmuyeong.remittance.presentation.response;

/**
 * 공통 API 응답 포맷
 */
public record ApiResponse<T>(T data) {
	public static <T> ApiResponse<T> of(T data) {
		return new ApiResponse<>(data);
	}
}