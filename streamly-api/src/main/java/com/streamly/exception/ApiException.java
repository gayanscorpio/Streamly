package com.streamly.exception;

import org.springframework.graphql.execution.ErrorType;

//Our own exception: carries an error category (UNAUTHORIZED, FORBIDDEN, NOT_FOUND) + a message
public class ApiException extends RuntimeException {
	final ErrorType type; // the category

	public ApiException(ErrorType type, String message) {
		super(message); // message shown to the user
		this.type = type;
	}
}
