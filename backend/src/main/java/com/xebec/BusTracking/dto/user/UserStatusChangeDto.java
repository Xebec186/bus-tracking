package com.xebec.BusTracking.dto.user;

import com.xebec.BusTracking.model.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserStatusChangeDto {

    @NotNull
    private Long targetUserId;

    @NotNull(message = "New status is required")
    private UserStatus newStatus;
}