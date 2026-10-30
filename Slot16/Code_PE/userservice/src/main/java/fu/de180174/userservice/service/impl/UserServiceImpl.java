package fu.de180174.userservice.service.impl;

import fu.de180174.userservice.common.DuplicateUsernameException;
import fu.de180174.userservice.common.ResourceNotFoundException;
import fu.de180174.userservice.common.ValidationException;
import fu.de180174.userservice.dto.PageDTO;
import fu.de180174.userservice.dto.UserDTO;
import fu.de180174.userservice.entity.Role;
import fu.de180174.userservice.entity.User;
import fu.de180174.userservice.entity.UserStatus;
import fu.de180174.userservice.repository.UserRepository;
import fu.de180174.userservice.service.UserService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        validateUserDTOForCreate(userDTO);

        if (userRepository.existsByUsername(userDTO.getUsername().trim())) {
            throw new DuplicateUsernameException("Duplicate username");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername().trim());
        user.setEmail(userDTO.getEmail().trim());
        user.setRole(parseRole(userDTO.getRole()));
        user.setRatePerPost(userDTO.getRatePerPost());
        user.setMaxDailyPosts(userDTO.getMaxDailyPosts());
        user.setLevel(userDTO.getLevel());
        user.setStatus(UserStatus.ACTIVE);

        User saved = userRepository.save(user);
        return mapToDTO(saved);
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (userDTO.getUsername() != null) {
            String newUsername = userDTO.getUsername().trim();
            if (newUsername.isEmpty() || newUsername.length() > 30) {
                throw new ValidationException("Validation failed: username must not be empty and maximum length is 30");
            }
            if (!newUsername.equalsIgnoreCase(user.getUsername())) {
                if (userRepository.existsByUsername(newUsername)) {
                    throw new DuplicateUsernameException("Duplicate username");
                }
            }
            user.setUsername(newUsername);
        }

        if (userDTO.getEmail() != null) {
            String newEmail = userDTO.getEmail().trim();
            if (newEmail.isEmpty() || newEmail.length() > 100 || !EMAIL_PATTERN.matcher(newEmail).matches()) {
                throw new ValidationException("Validation failed: invalid email format or maximum length 100 exceeded");
            }
            user.setEmail(newEmail);
        }

        if (userDTO.getRole() != null) {
            user.setRole(parseRole(userDTO.getRole()));
        }

        if (userDTO.getRatePerPost() != null) {
            if (userDTO.getRatePerPost().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Validation failed: ratePerPost must be greater than 0");
            }
            user.setRatePerPost(userDTO.getRatePerPost());
        }

        if (userDTO.getMaxDailyPosts() != null) {
            if (userDTO.getMaxDailyPosts() < 1 || userDTO.getMaxDailyPosts() > 10) {
                throw new ValidationException("Validation failed: maxDailyPosts must be between 1 and 10");
            }
            user.setMaxDailyPosts(userDTO.getMaxDailyPosts());
        }

        if (userDTO.getLevel() != null) {
            if (userDTO.getLevel() < 1) {
                throw new ValidationException("Validation failed: level must be greater than or equal to 1");
            }
            user.setLevel(userDTO.getLevel());
        }

        if (userDTO.getStatus() != null) {
            user.setStatus(parseUserStatus(userDTO.getStatus()));
        }

        User updated = userRepository.save(user);
        return mapToDTO(updated);
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDTO(user);
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.DEACTIVATED);
        userRepository.save(user);
    }

    @Override
    public PageDTO getUsers(Integer page, Integer size, String role, String status) {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;

        if (pageSize > 100 || pageSize < 1 || pageNum < 0) {
            throw new ValidationException("Validation failed: size must be between 1 and 100, page must be >= 0");
        }

        final Role roleFilter = (role != null && !role.trim().isEmpty()) ? parseRole(role) : null;
        final UserStatus statusFilter = (status != null && !status.trim().isEmpty()) ? parseUserStatus(status) : null;

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (roleFilter != null) {
                predicates.add(cb.equal(root.get("role"), roleFilter));
            }
            if (statusFilter != null) {
                predicates.add(cb.equal(root.get("status"), statusFilter));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, PageRequest.of(pageNum, pageSize));

        List<UserDTO> dtoList = userPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PageDTO(
                userPage.getSize(),
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.isFirst(),
                userPage.isLast(),
                dtoList
        );
    }

    private void validateUserDTOForCreate(UserDTO dto) {
        if (dto == null) {
            throw new ValidationException("Validation failed: request body is missing");
        }
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty() || dto.getUsername().trim().length() > 30) {
            throw new ValidationException("Validation failed: username is required and must not exceed 30 characters");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty() || dto.getEmail().trim().length() > 100 || !EMAIL_PATTERN.matcher(dto.getEmail().trim()).matches()) {
            throw new ValidationException("Validation failed: email is required, must be valid and must not exceed 100 characters");
        }
        if (dto.getRole() == null || dto.getRole().trim().isEmpty()) {
            throw new ValidationException("Validation failed: role is required (USER, CREATOR, ADMIN)");
        }
        parseRole(dto.getRole());

        if (dto.getRatePerPost() == null || dto.getRatePerPost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Validation failed: ratePerPost is required and must be greater than 0");
        }
        if (dto.getMaxDailyPosts() == null || dto.getMaxDailyPosts() < 1 || dto.getMaxDailyPosts() > 10) {
            throw new ValidationException("Validation failed: maxDailyPosts is required and must be between 1 and 10");
        }
        if (dto.getLevel() == null || dto.getLevel() < 1) {
            throw new ValidationException("Validation failed: level is required and must be greater than or equal to 1");
        }
    }

    private Role parseRole(String roleStr) {
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (Exception e) {
            throw new ValidationException("Validation failed: role must be USER, CREATOR, or ADMIN");
        }
    }

    private UserStatus parseUserStatus(String statusStr) {
        try {
            return UserStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (Exception e) {
            throw new ValidationException("Validation failed: status must be ACTIVE, SUSPENDED, or DEACTIVATED");
        }
    }

    private UserDTO mapToDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getRatePerPost(),
                user.getMaxDailyPosts(),
                user.getLevel(),
                user.getStatus().name()
        );
    }
}
