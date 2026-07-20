package com.markokosic.minicrm.exception;

//409 Ressource Conflict
public class ResourceConflictException extends ApiException {
	public ResourceConflictException(String i18nKey, Object... args) { super(i18nKey, args); }
}