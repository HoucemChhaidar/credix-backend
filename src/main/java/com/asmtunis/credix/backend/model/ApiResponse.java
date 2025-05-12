package com.asmtunis.credix.backend.model;

public class ApiResponse<T> {
	private final int status;
	private final String message;
	private final T data;

	public ApiResponse(String message, int status, T data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public T getData() {
		return data;
	}
}
