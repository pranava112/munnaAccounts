package com.vpm.Accounts.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MemberRequest {
    @NotBlank @Size(min = 3, max = 80) private String username;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8, max = 100) private String password;
    public String getUsername() { return username; } public void setUsername(String value) { username = value; }
    public String getEmail() { return email; } public void setEmail(String value) { email = value; }
    public String getPassword() { return password; } public void setPassword(String value) { password = value; }
}
