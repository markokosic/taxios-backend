package com.markokosic.minicrm.modules.auth;

import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.exception.ForbiddenException;
import com.markokosic.minicrm.modules.auth.dto.response.AuthResponseDTO;
import com.markokosic.minicrm.modules.auth.dto.response.RefreshAccessTokenResponseDTO;
import com.markokosic.minicrm.modules.auth.dto.response.RegisterTenantResponseDTO;
import com.markokosic.minicrm.modules.auth.config.TokenProperties;
import com.markokosic.minicrm.modules.auth.dto.request.LoginRequestDTO;
import com.markokosic.minicrm.modules.auth.dto.request.RegisterTenantRequestDTO;
import com.markokosic.minicrm.modules.auth.service.AuthService;
import com.markokosic.minicrm.modules.user.dto.response.UserResponseDTO;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenProperties tokenProperties;

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> getMe(){
        UserResponseDTO meResponse = authService.getMe();
        return ResponseEntity.ok(new ApiResponseDTO<>(true, meResponse, "Session is valid" ));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<RegisterTenantResponseDTO>> register (@Valid @RequestBody RegisterTenantRequestDTO userAndTenantDto){
        RegisterTenantResponseDTO registrationResponse =  authService.registerNewTenant(userAndTenantDto);

        return ResponseEntity.ok(new ApiResponseDTO<>(true, registrationResponse, "Successfully registered new tenant."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequest)  {
       AuthResponseDTO authResponse = authService.login(loginRequest);

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", authResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(tokenProperties.getAccess().getExpirationMinutes() * 60)
                .path("/")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(tokenProperties.getRefresh().getExpirationMinutes()*60)
                .path("/")
                .build();


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new ApiResponseDTO<>(true, authResponse, "Successfully logged in"));
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<ApiResponseDTO<RefreshAccessTokenResponseDTO>> refreshAccessToken(@CookieValue("refreshToken") String refreshToken){
        // TODO Refactor:
        // Remove try/catch and handle exceptions via @RestControllerAdvice.
        // Move cookie handling into the authentication service.
        // Handle AuthException and ValidationException via @RestControllerAdvice.
        // Keep this controller responsible only for request/response mapping.


        try {
            String accessToken = authService.refreshAccessToken(refreshToken);

            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .maxAge(tokenProperties.getAccess().getExpirationMinutes() * 60)
                    .path("/")
                    .build();

            RefreshAccessTokenResponseDTO responseDTO = new RefreshAccessTokenResponseDTO();
            responseDTO.setAccessToken(accessToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .body(new ApiResponseDTO<>(true, responseDTO, "Successfully refreshed Access Token"));

        } catch (ForbiddenException | ValidationException e) {
            ResponseCookie clearAccessToken = ResponseCookie.from("accessToken", "")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(0)
                    .build();

            ResponseCookie clearRefreshToken = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(0)
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, clearAccessToken.toString())
                    .header(HttpHeaders.SET_COOKIE, clearRefreshToken.toString())
                    .body(new ApiResponseDTO<>(false, null, "Session expired. Please login again."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<Void>> logout() {
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new ApiResponseDTO<>(true, null, "Successfully logged out"));
    }


}
