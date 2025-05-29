package com.asmtunis.credix.backend.common.dto;

public record ResponseWrapper<T>(int status, String message, T data) {}
