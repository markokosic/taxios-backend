package com.markokosic.minicrm.common.config.security.filter;


import com.markokosic.minicrm.common.config.security.CustomAuthenticationEntryPoint;
import com.markokosic.minicrm.modules.tenant.TenantContextHolder;
import com.markokosic.minicrm.modules.auth.service.JWTService;
import com.markokosic.minicrm.modules.auth.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;


@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final ApplicationContext context;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh-token")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        // 1. Token aus Cookies extrahieren
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }


        if (token != null) {
            try {
                String username = jwtService.extractEmail(token);
                Long tenantId = jwtService.extractTenantId(token);

                if (username != null && tenantId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = context.getBean(UserDetailsServiceImpl.class).loadUserByUsername(username);

                    if (jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        TenantContextHolder.setTenantId(tenantId);
                    }
                }
            } catch (ExpiredJwtException | io.jsonwebtoken.security.SignatureException | io.jsonwebtoken.MalformedJwtException e) {

                SecurityContextHolder.clearContext();
                TenantContextHolder.clear();

                authenticationEntryPoint.commence(
                        request,
                        response,
                        new BadCredentialsException("JWT ist abgelaufen oder ungültig", e)
                );
                return;
            } catch (Exception e) {

                SecurityContextHolder.clearContext();
                TenantContextHolder.clear();


                org.springframework.http.ResponseCookie deleteAccess = org.springframework.http.ResponseCookie.from("accessToken", "")
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("None")
                        .path("/")
                        .maxAge(0)
                        .build();


                org.springframework.http.ResponseCookie deleteRefresh = org.springframework.http.ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("None")
                        .path("/")
                        .maxAge(0)
                        .build();


                response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, deleteAccess.toString());
                response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, deleteRefresh.toString());

                authenticationEntryPoint.commence(
                        request,
                        response,
                        new BadCredentialsException("Allgemeiner Authentifizierungsfehler", e)
                );
                return;
            }
        }


        try {
            filterChain.doFilter(request, response);
        } finally {

            TenantContextHolder.clear();
        }
    }
}