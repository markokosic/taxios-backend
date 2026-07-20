package com.markokosic.minicrm.exception;

// 401 Unauthorized
public class UnauthorizedException extends ApiException {
	public UnauthorizedException(String i18nKey, Object... args) { super(i18nKey, args); }

}
