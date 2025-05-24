package org.example.prodcatservice.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.example.prodcatservice.dtos.product.responseDtos.TokenIntrospectionResponseDTO;
import org.example.prodcatservice.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RBACAspectTest {

    private HttpServletRequest request;
    private TokenService tokenService;
    private RBACAspect rbacAspect;
    private JoinPoint joinPoint;

    @BeforeEach
    void setup() {
        request = mock(HttpServletRequest.class);
        tokenService = mock(TokenService.class);
        joinPoint = mock(JoinPoint.class);
        rbacAspect = new RBACAspect(request, tokenService);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckScope_allowsAccess() {
        HasScope hasScope = new HasScope() {
            @Override
            public String value() {
                return "internal";
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return HasScope.class;
            }
        };

        TokenIntrospectionResponseDTO token = new TokenIntrospectionResponseDTO();
        token.setScopes(List.of("internal", "read"));

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenService.introspect("Bearer token")).thenReturn(token);

        // Should not throw
        rbacAspect.checkScope(joinPoint, hasScope);
    }

    @Test
    void testCheckScope_deniesAccess() {
        HasScope hasScope = new HasScope() {
            @Override
            public String value() {
                return "internal";
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return HasScope.class;
            }
        };

        TokenIntrospectionResponseDTO token = new TokenIntrospectionResponseDTO();
        token.setScopes(List.of("read"));

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenService.introspect("Bearer token")).thenReturn(token);

        assertThrows(AccessDeniedException.class, () -> rbacAspect.checkScope(joinPoint, hasScope));
    }

    @Test
    void testCheckAdminRole_allowsAccess() {
        TokenIntrospectionResponseDTO token = new TokenIntrospectionResponseDTO();
        token.setRoles(List.of("ADMIN", "USER"));

        when(request.getHeader("Authorization")).thenReturn("Bearer admin-token");
        when(tokenService.introspect("Bearer admin-token")).thenReturn(token);

        // Should not throw
        rbacAspect.checkAdminRole(joinPoint);
    }

    @Test
    void testCheckAdminRole_deniesAccess() {
        TokenIntrospectionResponseDTO token = new TokenIntrospectionResponseDTO();
        token.setRoles(List.of("USER"));

        when(request.getHeader("Authorization")).thenReturn("Bearer user-token");
        when(tokenService.introspect("Bearer user-token")).thenReturn(token);

        assertThrows(AccessDeniedException.class, () -> rbacAspect.checkAdminRole(joinPoint));
    }
}
