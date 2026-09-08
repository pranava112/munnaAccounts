package com.vpm.Accounts.DTO;
public record TokenResponse(
    String accessToken, 
    String refreshToken, 
    Long tenantId, 
    String username, 
    String role) {}
