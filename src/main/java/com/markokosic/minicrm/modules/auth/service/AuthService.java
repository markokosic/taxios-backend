package com.markokosic.minicrm.modules.auth.service;


import com.markokosic.minicrm.common.error.ApiErrorCode;
import com.markokosic.minicrm.modules.auth.config.TokenProperties;
import com.markokosic.minicrm.modules.auth.dto.request.LoginRequestDTO;
import com.markokosic.minicrm.modules.auth.dto.request.RegisterTenantRequestDTO;
import com.markokosic.minicrm.modules.auth.dto.response.AuthResponseDTO;
import com.markokosic.minicrm.modules.auth.dto.response.RegisterTenantResponseDTO;
import com.markokosic.minicrm.modules.user.dto.response.UserResponseDTO;
import com.markokosic.minicrm.exception.AuthException;
import com.markokosic.minicrm.exception.ValidationException;
import com.markokosic.minicrm.modules.tenant.Tenant;
import com.markokosic.minicrm.modules.user.User;
import com.markokosic.minicrm.modules.auth.model.UserPrincipal;
import com.markokosic.minicrm.modules.tenant.TenantRepository;
import com.markokosic.minicrm.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//TODO 1. Logout fn and remove cookies, 2. if token expired remove cookies

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final ApplicationContext applicationContext;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenProperties tokenProperties;

    @Transactional
    public RegisterTenantResponseDTO registerNewTenant(RegisterTenantRequestDTO userAndTenantDto) {

        Tenant savedTenant = createTenant(userAndTenantDto.getTenantName());
        createUser(userAndTenantDto, savedTenant);

        return new RegisterTenantResponseDTO(savedTenant.getId(), savedTenant.getName());
    }

    public Tenant createTenant (String name) {

        if(tenantRepository.existsByName(name)){
            throw new ValidationException(ApiErrorCode.TENANT_NAME_DUPLICATE);
        }

        Tenant tenant = new Tenant();
        tenant.setName(name);
         return tenantRepository.save(tenant);
    }

    public void createUser (RegisterTenantRequestDTO request, Tenant tenant) {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new ValidationException(ApiErrorCode.VALIDATION_EMAIL_DUPLICATE);
        }

        User newUser = new User();
        newUser.setTenantId(tenant.getId());
        newUser.setEmail(request.getEmail());
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(newUser);
    }




    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());

        if (optionalUser.isEmpty()) {
            throw new AuthException(ApiErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        User user = optionalUser.get();

        try {
             authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            String accessToken = jwtService.generateToken(loginRequest.getEmail(), user.getTenantId(), tokenProperties.getAccess().getExpirationMinutes());
            String refreshToken = jwtService.generateToken(loginRequest.getEmail(), user.getTenantId(), tokenProperties.getRefresh().getExpirationMinutes());
            UserResponseDTO userResponseDTO = new UserResponseDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());

            return new AuthResponseDTO(accessToken, refreshToken, userResponseDTO);

        } catch (AuthenticationException ex) {
            throw new AuthException(ApiErrorCode.AUTH_INVALID_CREDENTIALS);

        }
    }



    public UserResponseDTO getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        UserResponseDTO userDto = new UserResponseDTO();
        userDto.setId(principal.getId());
        userDto.setEmail(principal.getEmail());
        return userDto;
    }

    public String refreshAccessToken(String refreshToken){
        //1. checkk if refreshToken is received
        if(refreshToken.isEmpty()){
            throw new ValidationException(ApiErrorCode.AUTH_NO_TOKEN_RECEIVED);
        }

        //2 validate refresh token, if not valid return UNAUTH, please login again
        boolean isSignedAndValid = jwtService.validateRefreshToken(refreshToken);
        if(!isSignedAndValid){
            throw new AuthException(ApiErrorCode.AUTH_TOKEN_EXPIRED);
        };

        String username = jwtService.extractEmail(refreshToken);
        Long tenantId = jwtService.extractTenantId(refreshToken);
        return jwtService.generateToken(username, tenantId, tokenProperties.getAccess().getExpirationMinutes());

    }

}
