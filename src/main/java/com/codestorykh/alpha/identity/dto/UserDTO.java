package com.codestorykh.alpha.identity.dto;

import com.codestorykh.alpha.common.dto.BaseDTO;
import com.codestorykh.alpha.identity.domain.UserStatus;
import com.codestorykh.alpha.validation.annotation.StrongPassword;
import com.codestorykh.alpha.validation.annotation.ValidPhoneNumber;
import com.codestorykh.alpha.validation.annotation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO extends BaseDTO {

    @NotBlank(message = "Username is required")
    @ValidUsername(minLength = 3, maxLength = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @StrongPassword(minLength = 8)
    private String password;

    private String firstName;
    private String lastName;
    
    @ValidPhoneNumber(countryCode = "", allowInternational = true)
    private String phoneNumber;
    
    private UserStatus status;
    private boolean enabled;
    private boolean emailVerified;
    private boolean phoneVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime passwordChangedAt;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private Set<String> roles;
    private Set<String> groups;
    private Set<String> permissions;
} 