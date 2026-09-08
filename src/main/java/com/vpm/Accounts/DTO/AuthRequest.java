package com.vpm.Accounts.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRequest {
    @NotBlank @Size(min = 3, max = 80) private String username;
    @NotBlank @Size(min = 8, max = 100) private String password;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 2, max = 120) private String companyName;
    public String getUsername() { return username; } public void setUsername(String value) { username = value; }
    public String getPassword() { return password; } public void setPassword(String value) { password = value; }
    public String getEmail() { return email; } public void setEmail(String value) { email = value; }
    public String getCompanyName() { return companyName; } public void setCompanyName(String value) { companyName = value; }
}
