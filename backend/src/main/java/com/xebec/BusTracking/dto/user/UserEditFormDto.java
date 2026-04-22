package com.xebec.BusTracking.dto.user;

import com.xebec.BusTracking.model.UserStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEditFormDto {

    @NotNull
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Size(max = 150)
    private String email;

    /**
     * Optional – if blank, password is NOT changed.
     * If provided, must meet strength requirements.
     */
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    @Pattern(
            regexp = "^$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain uppercase, lowercase, and a digit"
    )
    private String newPassword;

    private String confirmNewPassword;

    /**
     * Role is NOT on this form – role changes go through UserRoleChangeDto
     * via a separate, audited endpoint to enforce the self-change prohibition.
     */

    @NotNull(message = "Status is required")
    private UserStatus status;
}