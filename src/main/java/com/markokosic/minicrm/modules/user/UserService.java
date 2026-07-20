package com.markokosic.minicrm.modules.user;

import com.markokosic.minicrm.exception.ResourceNotFoundException;
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

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("domain.user.not_found"));
        return convertToUserResponseDto(user);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("domain.user.not_found"));
        userRepository.delete(user);
    }

    public UserResponseDTO convertToUserResponseDto(User user) {
        return userMapper.userToUserResponseDTO(user);
    }
}
