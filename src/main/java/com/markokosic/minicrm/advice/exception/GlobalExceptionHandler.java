package com.markokosic.minicrm.advice.exception;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ErrorResponseDTO;
import com.markokosic.minicrm.common.error.ApiErrorCode;
import com.markokosic.minicrm.exception.ApiException;
import com.markokosic.minicrm.exception.AuthException;
import com.markokosic.minicrm.exception.BusinessException;
import com.markokosic.minicrm.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	private final I18nService i18n;

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponseDTO> handleApiException(ApiException ex) {
		return buildError(ex.getErrorCode(), ex.getStatus());
	}

	@ExceptionHandler(AuthException.class)
	public ResponseEntity<ErrorResponseDTO> handleAuthException(AuthException ex) {
		return buildError(ex.getErrorCode(), ex.getStatus());
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleNotFoundException(NotFoundException ex) {
		return buildError(ex.getErrorCode(), ex.getStatus());
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex) {
		return buildError(ex.getErrorCode(), ex.getStatus());
	}


	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDTO> handleOtherExceptions(Exception ex) {
		return buildError(ApiErrorCode.AUTH_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<ErrorResponseDTO> buildError(ApiErrorCode code, HttpStatusCode status) {
		ErrorResponseDTO error = ErrorResponseDTO.builder()
				.statusCode(status)
				.message(i18n.getMessage(code.getKey()))
				.errorKey(code.getKey())
				.timestamp(Instant.now())
				.build();
		return ResponseEntity.status(status).body(error);
	}

}
