package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.PagedResult;
import com.xebec.BusTracking.dto.TokenResponse;
import com.xebec.BusTracking.dto.user.*;
import com.xebec.BusTracking.exception.*;
import com.xebec.BusTracking.model.User;
import com.xebec.BusTracking.model.UserRole;
import com.xebec.BusTracking.model.UserStatus;
import com.xebec.BusTracking.repository.UserRepository;
import com.xebec.BusTracking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ── LOGIN METHOD ────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public TokenResponse login(LoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            throw new ResourceNotFoundException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccessDeniedException("User account is " + user.getStatus().label());
        }

        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new TokenResponse(token, refreshToken);
    }

    // ── SIGNUP METHOD FOR REST API ────────────────────────────────────────────────────────────
    @Override
    public User signup(SignupDto signupDto) throws EmailAlreadyExistsException {
        String email = signupDto.getEmail();
        if(userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("User already exists with given email: " + email);
        }

        User user = User.builder()
                .firstName(signupDto.getFirstName())
                .lastName(signupDto.getLastName())
                .phoneNumber(signupDto.getPhoneNumber())
                .email(signupDto.getEmail())
                .role(UserRole.PASSENGER)
                .passwordHash(passwordEncoder.encode(signupDto.getPassword()))
                .build();

        return userRepository.save(user);
    }

    private UserDto toDto(User u) {
        return UserDto.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .role(u.getRole())
                .status(u.getStatus())
                .createdAt(u.getCreatedAt())
                .build();
    }

    // ── LIST ────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public PagedResult<UserListItemDto> findFiltered(UserFilterDto filter) {
        Pageable pageable = PageRequest.of(
                filter.getPage(), filter.getSize(),
                Sort.by(Sort.Direction.ASC, "lastName", "firstName"));

        Page<User> page = userRepository.findFiltered(
                filter.getSearch(),
                filter.getRole(),
                filter.getStatus(),
                pageable);

        List<UserListItemDto> content = page.getContent()
                .stream().map(this::toListItem).collect(Collectors.toList());

        return new PagedResult<>(content, filter.getPage(),
                page.getTotalPages(), page.getTotalElements(), filter.getSize());
    }

    // ── EDIT FORM PRE-FILL ───────────────────────────────────────────────
    @Override
    public UserEditFormDto findEditFormById(Long id) {
        User u = findOrThrow(id);
        return UserEditFormDto.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .status(u.getStatus())
                .build();
    }

    // ── CREATE ───────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void create(UserCreateFormDto form) {
        // 1. Password match
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new PasswordsDoNotMatchException();
        }
        // 2. Unique email
        if (userRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new EmailAlreadyExistsException(form.getEmail());
        }
        User user = new User();
        user.setFirstName(form.getFirstName().trim());
        user.setLastName(form.getLastName().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(form.getRole());
        user.setStatus(form.getStatus());
        userRepository.save(user);
        log.info("Admin created user {} ({})", user.getEmail(), user.getRole());
    }

    // ── UPDATE PROFILE ───────────────────────────────────────────────────
    @Override
    @Transactional
    public void update(UserEditFormDto form, Long currentAdminId) {
        User user = findOrThrow(form.getId());

        // If changing email, verify it is still unique
        if (!user.getEmail().equalsIgnoreCase(form.getEmail())
                && userRepository.existsByEmailIgnoreCaseAndIdNot(form.getEmail(), form.getId())) {
            throw new EmailAlreadyExistsException(form.getEmail());
        }

        // Optional password change
        if (form.getNewPassword() != null && !form.getNewPassword().isBlank()) {
            if (!form.getNewPassword().equals(form.getConfirmNewPassword())) {
                throw new PasswordsDoNotMatchException();
            }
            user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
            log.info("Admin {} reset password for user {}", currentAdminId, user.getId());
        }

        user.setFirstName(form.getFirstName().trim());
        user.setLastName(form.getLastName().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setStatus(form.getStatus());
        userRepository.save(user);
    }

    // ── ROLE CHANGE ──────────────────────────────────────────────────────
    @Override
    @Transactional
    public void changeRole(UserRoleChangeDto dto, Long currentAdminId) {
        // ✦ Core privilege-abuse guard
        if (currentAdminId.equals(dto.getTargetUserId())) {
            throw new SelfPrivilegeChangeException(
                    "Administrators cannot change their own role.");
        }
        User target = findOrThrow(dto.getTargetUserId());
        UserRole previous = target.getRole();
        target.setRole(dto.getNewRole());
        userRepository.save(target);
        log.warn("Admin {} changed role of user {} from {} to {}",
                currentAdminId, target.getId(), previous, dto.getNewRole());
    }

    // ── STATUS CHANGE ────────────────────────────────────────────────────
    @Override
    @Transactional
    public void changeStatus(UserStatusChangeDto dto, Long currentAdminId) {
        if (currentAdminId.equals(dto.getTargetUserId())) {
            throw new SelfPrivilegeChangeException(
                    "Administrators cannot change their own account status.");
        }
        User target = findOrThrow(dto.getTargetUserId());
        target.setStatus(dto.getNewStatus());
        userRepository.save(target);
        log.info("Admin {} changed status of user {} to {}",
                currentAdminId, target.getId(), dto.getNewStatus());
    }

    // ── DELETE ───────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void delete(Long targetUserId, Long currentAdminId) {
        if (currentAdminId.equals(targetUserId)) {
            throw new SelfDeleteException();
        }
        User target = findOrThrow(targetUserId);
        userRepository.delete(target);
        log.warn("Admin {} deleted user {} ({})",
                currentAdminId, target.getId(), target.getEmail());
    }

    // ── STATS ────────────────────────────────────────────────────────────
    @Override
    public long countByRole(UserRole role)  { return userRepository.countByRole(role); }
    @Override
    public long countTotal()            { return userRepository.count(); }
    @Override
    public long countActive()           { return userRepository.countByStatus(UserStatus.ACTIVE); }

    /* ── private helpers ── */
    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given id: " + id));
    }

    private UserListItemDto toListItem(User u) {
        return UserListItemDto.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .role(u.getRole())
                .status(u.getStatus())
                .createdAt(u.getCreatedAt())
                .lastLoginAt(u.getLastLoginAt())
                .build();
    }
}
