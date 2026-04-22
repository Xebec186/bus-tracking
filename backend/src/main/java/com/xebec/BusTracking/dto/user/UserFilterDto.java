package com.xebec.BusTracking.dto.user;

import com.xebec.BusTracking.model.UserRole;
import com.xebec.BusTracking.model.UserStatus;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserFilterDto {
    private String     search;      // matches first name, last name, or email
    private UserRole       role;
    private UserStatus status;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 20;
}