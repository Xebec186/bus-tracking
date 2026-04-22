package com.xebec.BusTracking.dto.user;

import com.xebec.BusTracking.model.UserRole;
import com.xebec.BusTracking.model.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserListItemDto {
    private Long          id;
    private String        firstName;
    private String        lastName;
    private String        email;
    private UserRole          role;
    private UserStatus    status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    /** Convenience – avoids string concat in Thymeleaf */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** Initials for avatar fallback */
    public String getInitials() {
        String f = (firstName != null && !firstName.isEmpty()) ? String.valueOf(firstName.charAt(0)) : "";
        String l = (lastName  != null && !lastName.isEmpty())  ? String.valueOf(lastName.charAt(0))  : "";
        return (f + l).toUpperCase();
    }
}