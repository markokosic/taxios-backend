package com.markokosic.minicrm.exception;

// 400 Bad Request
public class BadRequestException extends ApiException {
	public BadRequestException(String i18nKey, Object... args) { super(i18nKey, args); }
}