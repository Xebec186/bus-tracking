package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.PagedResult;
import com.xebec.BusTracking.dto.user.*;
import com.xebec.BusTracking.model.User;

public interface UserService {
    public User signup(SignupDto signupDto);

    /** Paginated, filtered user list for the admin table */
    PagedResult<UserListItemDto> findFiltered(UserFilterDto filter);

    /** Single user for pre-filling the edit modal */
    UserEditFormDto findEditFormById(Long id);

    /**
     * Create a new user.
     * Validates: unique email, password == confirmPassword.
     * Throws {@link com.xebec.BusTracking.exception.EmailAlreadyExistsException}
     * if email already exists.
     */
    void create(UserCreateFormDto form);

    /**
     * Update profile fields (name, email, optional password, status).
     * Role is NOT changed here.
     */
    void update(UserEditFormDto form, Long currentAdminId);

    /**
     * Change a user's role.
     * Throws {@link com.xebec.BusTracking.exception.SelfPrivilegeChangeException}
     * if currentAdminId == targetUserId.
     */
    void changeRole(UserRoleChangeDto dto, Long currentAdminId);

    /**
     * Activate / suspend / deactivate a user.
     * Administrators cannot suspend their own account.
     */
    void changeStatus(UserStatusChangeDto dto, Long currentAdminId);

    /**
     * Hard-delete a user.
     * Throws {@link com.xebec.BusTracking.exception.SelfDeleteException}
     * if currentAdminId == targetUserId.
     */
    void delete(Long targetUserId, Long currentAdminId);

    /** Quick stats for the page header pills */
    long countByRole(com.xebec.BusTracking.model.UserRole role);
    long countTotal();
    long countActive();
}
