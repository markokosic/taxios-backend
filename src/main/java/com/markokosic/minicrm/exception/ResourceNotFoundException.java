package com.markokosic.minicrm.exception;

// 404 Not Found
public class ResourceNotFoundException extends ApiException {
	public ResourceNotFoundException(String i18nKey, Object... args) { super(i18nKey, args); }
}