package com.vpm.Accounts.DTO;

import jakarta.validation.constraints.NotBlank;
public class LoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    public String getUsername() { return username; } public void setUsername(String value) { username = value; }
    public String getPassword() { return password; } public void setPassword(String value) { password = value; }
}
