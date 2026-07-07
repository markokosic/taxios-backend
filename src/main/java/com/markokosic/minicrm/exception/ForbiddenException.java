package com.markokosic.minicrm.exception;

// 403 Forbidden
public class ForbiddenException extends ApiException {
	public ForbiddenException(String i18nKey, Object... args) { super(i18nKey, args); }
}