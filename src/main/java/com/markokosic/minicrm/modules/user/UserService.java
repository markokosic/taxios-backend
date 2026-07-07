package com.markokosic.minicrm.modules.user;

import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.tenant.TenantService;
import com.markokosic.minicrm.modules.user.dto.response.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final TenantService tenantService;

    public UserResponseDTO getUserById(Long id) {
        Long tenantId = tenantService.getTenantIdFromContextHolder();
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("domain.user.not_found"));
        return convertToUserResponseDto(user);
    }

    public List<UserResponseDTO> getAllUsers() {
        Long tenantId = tenantService.getTenantIdFromContextHolder();
        return userRepository.findAllByTenantId(tenantId).stream()
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        Long tenantId = tenantService.getTenantIdFromContextHolder();
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("domain.user.not_found"));
        userRepository.delete(user);
    }

    public UserResponseDTO convertToUserResponseDto(User user) {
        return userMapper.userToUserResponseDTO(user);
    }
}
