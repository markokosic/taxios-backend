package com.markokosic.minicrm.common.error;

public enum ApiErrorCode {
	// Authentication Errors
	AUTH_INVALID_CREDENTIALS("auth.invalid-credentials", "The email or password you entered is incorrect"),
	AUTH_TOKEN_EXPIRED("auth.token.expired", "Token has expired"),
	AUTH_REGISTRATION_FAILED("auth.registration.failed", "Registration failed"),
	AUTH_NO_TOKEN_RECEIVED("auth.token.no-token-received", "No token received, please login again."),

	// Validation Errors
	VALIDATION_FIELD_BLANK("validation.field-blank", "Field cannot be blank"),
	VALIDATION_EMAIL_REQUIRED("validation.email.required", "Email is required"),
	VALIDATION_EMAIL_INVALID("validation.email.invalid", "Email is invalid"),
	VALIDATION_EMAIL_DUPLICATE("validation.email.duplicate", "Email already in use. Please use another email address."),
	VALIDATION_PASSWORD_REQUIRED("validation.password.required", "Password is required"),
	VALIDATION_PASSWORD_TOO_SHORT("validation.password.too-short", "Password must be at least 8 characters"),
	VALIDATION_MIN_SIZE_3("validation.min-size-3", "Must be at least 3 characters"),
	VALIDATION_MIN_SIZE_8("validation.min-size-8", "Must be at least 8 characters"),
	VALIDATION_MAX_SIZE_15("validation.max-size-15", "Must be at most 15 characters"),
	VALIDATION_MAX_SIZE_100("validation.max-size-100", "Must be at most 100 characters"),
	VALIDATION_INVALID_FIELD("validation.invalid-field", "One or more fields are not allowed for this customer type"),

	// Tenant Errors
	TENANT_NAME_DUPLICATE("tenant.duplicate", "Workspace name is already in use."),

	//User Errors
	USER_NOT_FOUND("user.not-found", "User not found"),

	//Customer Errors
	CUSTOMER_NOT_FOUND("customer.not-found","Customer not found"),

	//Driver Errors
	Driver_NOT_FOUND("driver.not-found","Driver not found"),


	//Car Errors
	CAR_NOT_FOUND("car.not-found","Car not found"),


	//Address Errors
	ADDRESS_NOT_FOUND("address.not-found","Address not found");


	private final String key;
	private final String message;

	ApiErrorCode(String key, String message) {
		this.key = key;
		this.message = message;
	}

	public String getKey() {
		return key;
	}

	public String getMessage() {
		return message;
	}
}
