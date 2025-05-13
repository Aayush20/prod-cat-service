package org.example.prodcatservice.configs;

import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtRateLimitingFilter implements Filter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) request;
        String path = http.getRequestURI();
        String subject = http.getHeader("Authorization"); // fallback — better if extracted earlier

        if (path.contains("/update-stock")) {
            Bucket bucket = cache.computeIfAbsent(subject, k -> Bucket4j.builder()
                    .addLimit(Bandwidth.simple(5, Duration.ofMinutes(1))) // 5 req/min
                    .build());

            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                ((HttpServletResponse) response).setStatus(429);
                response.getWriter().write("Too many requests");
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
