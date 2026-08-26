package com.markokosic.minicrm.modules.auth.service;

import com.markokosic.minicrm.exception.ResourceConflictException;
import com.markokosic.minicrm.exception.UnauthorizedException;
import com.markokosic.minicrm.modules.auth.config.TokenProperties;
import com.markokosic.minicrm.modules.auth.dto.request.LoginRequestDTO;
import com.markokosic.minicrm.modules.auth.dto.request.RegisterTenantRequestDTO;
import com.markokosic.minicrm.modules.auth.dto.response.AuthResponseDTO;
import com.markokosic.minicrm.modules.auth.dto.response.MeResponseDTO;
import com.markokosic.minicrm.modules.auth.dto.response.RegisterTenantResponseDTO;
import com.markokosic.minicrm.modules.auth.model.UserPrincipal;
import com.markokosic.minicrm.modules.tenant.Tenant;
import com.markokosic.minicrm.modules.tenant.TenantRepository;
import com.markokosic.minicrm.modules.user.User;
import com.markokosic.minicrm.modules.user.UserMapper;
import com.markokosic.minicrm.modules.user.UserRepository;
import com.markokosic.minicrm.modules.user.UserService;
import com.markokosic.minicrm.modules.user.dto.response.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//TODO 1. Logout fn and remove cookies, 2. if token expired remove cookies

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenProperties tokenProperties;
    private final UserMapper userMapper;

    @Transactional
    public RegisterTenantResponseDTO registerNewTenant(RegisterTenantRequestDTO userAndTenantDto) {
        Tenant savedTenant = createTenant(userAndTenantDto.getTenantName());
        userService.createTenantOwner(userAndTenantDto, savedTenant.getId());

        return new RegisterTenantResponseDTO(savedTenant.getId(), savedTenant.getName());
    }

    public Tenant createTenant(String name) {
        if (tenantRepository.existsByName(name)) {
            throw new ResourceConflictException("domain.tenant.name.duplicate");
        }

        Tenant tenant = new Tenant();
        tenant.setName(name);
        return tenantRepository.save(tenant);
    }

    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());

        if (optionalUser.isEmpty()) {
            throw new UnauthorizedException("auth.invalid_credentials");
        }

        User user = optionalUser.get();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            String accessToken = jwtService.generateToken(loginRequest.getEmail(), user.getTenantId(), tokenProperties.getAccess().getExpirationMinutes());
            String refreshToken = jwtService.generateToken(loginRequest.getEmail(), user.getTenantId(), tokenProperties.getRefresh().getExpirationMinutes());
            UserResponseDTO userResponseDTO = new UserResponseDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRoles(), user.isMustChangePassword());

            return new AuthResponseDTO(accessToken, refreshToken, userResponseDTO);

        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("auth.invalid_credentials");
        }
    }

    public MeResponseDTO getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new UnauthorizedException("auth.invalid_credentials"));

        String tenantName = tenantRepository.findById(user.getTenantId())
                .map(Tenant::getName)
                .orElse(null);

        return MeResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .tenantId(user.getTenantId())
                .tenantName(tenantName)
                .build();
    }

    public String refreshAccessToken(String refreshToken) {
        //1. check if refreshToken is received
        if (refreshToken.isEmpty()) {
            throw new UnauthorizedException("auth.token.no-token-received");
        }

        //2 validate refresh token, if not valid return UNAUTH, please login again
        boolean isSignedAndValid = jwtService.validateRefreshToken(refreshToken);
        if (!isSignedAndValid) {
            throw new UnauthorizedException("auth.token.expired");
        }

        String username = jwtService.extractEmail(refreshToken);
        Long tenantId = jwtService.extractTenantId(refreshToken);
        return jwtService.generateToken(username, tenantId, tokenProperties.getAccess().getExpirationMinutes());
    }
}
