package org.example.prodcatservice.utils;

import org.example.prodcatservice.dtos.product.responseDtos.TokenIntrospectionResponseDTO;

public class TokenClaimUtils {

    public static boolean hasScope(TokenIntrospectionResponseDTO token, String scope) {
        return token.getScopes() != null && token.getScopes().contains(scope);
    }

    public static boolean hasRole(TokenIntrospectionResponseDTO token, String role) {
        return token.getRoles() != null && token.getRoles().contains(role);
    }

    public static boolean isSystemCall(TokenIntrospectionResponseDTO token, String serviceSub) {
        return token.getSub() != null && token.getSub().equals(serviceSub);
    }
}
