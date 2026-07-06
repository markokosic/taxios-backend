package com.markokosic.minicrm.exception;

import com.markokosic.minicrm.common.I18nService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalRestExceptionHandler extends ResponseEntityExceptionHandler {
	private final I18nService i18n;

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request) {

		ProblemDetail body = ex.getBody();
		body.setTitle(i18n.getMessage("validation.title"));
		body.setDetail(i18n.getMessage("validation.detail"));

		Map<String, List<String>> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error ->
				errors.computeIfAbsent(error.getField(), k -> new ArrayList<>())
						.add(error.getDefaultMessage())
		);

		body.setProperty("errors", errors);

		return handleExceptionInternal(ex, body, headers, status, request);
	}

	@ExceptionHandler(InsufficientAuthenticationException.class)
	public ProblemDetail handleInsufficientAuthentication(InsufficientAuthenticationException ex) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.UNAUTHORIZED,
				i18n.getMessage("auth.unauthorized")
		);
		problemDetail.setTitle("Unauthorized");
		return problemDetail;
	}

	@ExceptionHandler(ApiException.class)
	public ProblemDetail handleDomainExceptions(ApiException ex) {
		HttpStatus status = switch (ex) {
			case ResourceNotFoundException e -> HttpStatus.NOT_FOUND;
			case ResourceConflictException e -> HttpStatus.CONFLICT;
			case ForbiddenException e         -> HttpStatus.FORBIDDEN;
			case UnauthorizedException e      -> HttpStatus.UNAUTHORIZED;
			case BadRequestException e        -> HttpStatus.BAD_REQUEST;
			default                           -> HttpStatus.BAD_REQUEST;
		};

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				status,
				i18n.getMessage(ex.getI18nKey(), ex.getArgs())
		);
		problemDetail.setTitle(status.getReasonPhrase());
		problemDetail.setProperty("error_code", ex.getI18nKey());
		return problemDetail;
	}


	@ExceptionHandler(Exception.class)
	public ProblemDetail handleOtherExceptions(Exception ex) {
		log.error("Unerwarteter Fehler: ", ex);

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR,
				i18n.getMessage("system.internal_server_error")
		);
		problemDetail.setTitle("Internal Server Error");
		return problemDetail;
	}

}
