package org.example.prodcatservice.services;

import org.example.prodcatservice.clients.AuthClient;

import org.example.prodcatservice.dtos.product.responseDtos.TokenIntrospectionResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Autowired
    private AuthClient authClient;

    public TokenIntrospectionResponseDTO introspect(String token) {
        return authClient.validateToken(token);
    }
}
