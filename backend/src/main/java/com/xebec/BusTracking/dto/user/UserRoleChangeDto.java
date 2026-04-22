package com.xebec.BusTracking.dto.user;

import com.xebec.BusTracking.model.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Sent via a dedicated POST endpoint, so role mutations are always
 * explicitly audited and subject to the self-change guard.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRoleChangeDto {

    @NotNull
    private Long targetUserId;

    @NotNull(message = "New role is required")
    private UserRole newRole;
}