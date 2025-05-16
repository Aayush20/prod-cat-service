package org.example.prodcatservice.clients;

import org.example.prodcatservice.dtos.product.responseDtos.TokenIntrospectionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @PostMapping(value = "/auth/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    TokenIntrospectionResponseDTO validateToken(@RequestHeader("Authorization") String token);
}
