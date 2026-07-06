package com.markokosic.minicrm.exception;

import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {
	private final String i18nKey;
	private final Object[] args;

	protected ApiException(String i18nKey, Object... args) {
		super(i18nKey);
		this.i18nKey = i18nKey;
		this.args = args;
	}
}