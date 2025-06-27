package com.shuaibu.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;

    @NotEmpty(message = "* Username is mandatory")
    private String username;

    @NotEmpty(message = "* Password is mandatory")
    private String password;

    private String fullName;
    private String isActive;
    private String role;
}
