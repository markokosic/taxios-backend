package com.markokosic.minicrm.exception;

import com.markokosic.minicrm.common.I18nService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalRestExceptionHandlerTest {

    @Mock
    private I18nService i18n;

    @InjectMocks
    private GlobalRestExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        when(i18n.getMessage(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(i18n.getMessage(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void handleInsufficientAuthentication_ReturnsUnauthorized() {
        var ex = new InsufficientAuthenticationException("Access denied");
        ProblemDetail pd = exceptionHandler.handleInsufficientAuthentication(ex);

        assertNotNull(pd);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), pd.getStatus());
        assertEquals("Unauthorized", pd.getTitle());
    }

    @Test
    void handleDomainExceptions_ResourceNotFound() {
        var ex = new ResourceNotFoundException("resource.not_found", 1L);
        ProblemDetail pd = exceptionHandler.handleDomainExceptions(ex);

        assertNotNull(pd);
        assertEquals(HttpStatus.NOT_FOUND.value(), pd.getStatus());
        assertEquals("Not Found", pd.getTitle());
    }

    @Test
    void handleDomainExceptions_ResourceConflict() {
        var ex = new ResourceConflictException("resource.conflict");
        ProblemDetail pd = exceptionHandler.handleDomainExceptions(ex);

        assertNotNull(pd);
        assertEquals(HttpStatus.CONFLICT.value(), pd.getStatus());
        assertEquals("Conflict", pd.getTitle());
    }

    @Test
    void handleDomainExceptions_Forbidden() {
        var ex = new ForbiddenException("error.forbidden");
        ProblemDetail pd = exceptionHandler.handleDomainExceptions(ex);

        assertNotNull(pd);
        assertEquals(HttpStatus.FORBIDDEN.value(), pd.getStatus());
        assertEquals("Forbidden", pd.getTitle());
    }

    @Test
    void handleDomainExceptions_Unauthorized() {
        var ex = new UnauthorizedException("error.unauthorized");
        ProblemDetail pd = exceptionHandler.handleDomainExceptions(ex);

        assertNotNull(pd);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), pd.getStatus());
        assertEquals("Unauthorized", pd.getTitle());
    }

    @Test
    void handleDomainExceptions_BadRequest() {
        var ex = new BadRequestException("error.bad_request");
        ProblemDetail pd = exceptionHandler.handleDomainExceptions(ex);

        assertNotNull(pd);
        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("Bad Request", pd.getTitle());
    }

    @Test
    void handleOtherExceptions_ReturnsInternalServerError() {
        var ex = new RuntimeException("Unexpected error");
        ProblemDetail pd = exceptionHandler.handleOtherExceptions(ex);

        assertNotNull(pd);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), pd.getStatus());
        assertEquals("Internal Server Error", pd.getTitle());
    }
}
