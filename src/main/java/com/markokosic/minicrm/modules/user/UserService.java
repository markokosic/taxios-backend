package com.markokosic.minicrm.modules.user;

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
        return null;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
    }


    public UserResponseDTO convertToUserResponseDto(User user) {
        return userMapper.userToUserResponseDTO(user);
    }


}
