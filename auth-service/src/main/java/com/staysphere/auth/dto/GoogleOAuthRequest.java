package com.staysphere.auth.dto;

import lombok.Data;

@Data
public class GoogleOAuthRequest {
    /** Google Identity Services ID token (credential). */
    private String idToken;
}
