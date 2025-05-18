package org.example.prodcatservice.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.prodcatservice.dtos.product.responseDtos.TokenIntrospectionResponseDTO;
import org.example.prodcatservice.services.TokenService;
import org.example.prodcatservice.utils.TokenClaimUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RBACAspect {

    private final HttpServletRequest request;
    private final TokenService tokenService;

    public RBACAspect(HttpServletRequest request, TokenService tokenService) {
        this.request = request;
        this.tokenService = tokenService;
    }

    @Before("@annotation(hasScope)")
    public void checkScope(JoinPoint joinPoint, HasScope hasScope) {
        String requiredScope = hasScope.value();
        String authHeader = request.getHeader("Authorization");

        TokenIntrospectionResponseDTO token = tokenService.introspect(authHeader);
        if (!TokenClaimUtils.hasScope(token, requiredScope)) {
            throw new AccessDeniedException("Missing required scope: " + requiredScope);
        }
    }

    @Before("@annotation(AdminOnly)")
    public void checkAdminRole(JoinPoint joinPoint) {
        String authHeader = request.getHeader("Authorization");
        TokenIntrospectionResponseDTO token = tokenService.introspect(authHeader);
        if (!TokenClaimUtils.hasRole(token, "ADMIN")) {
            throw new AccessDeniedException("Only ADMINs are allowed.");
        }
    }
}
