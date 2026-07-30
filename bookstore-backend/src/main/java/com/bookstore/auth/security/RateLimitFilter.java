package com.bookstore.auth.security;

import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Set;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> AUTH_RATE_LIMITED_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/forgot-password",
            "/api/auth/resend-verification",
            "/api/auth/reset-password"
    );

    private static final String CONTENT_PATH_PREFIX = "/api/content/";

    private final RateLimiterService rateLimiterService;
    private final JsonMapper jsonMapper;

    public RateLimitFilter(RateLimiterService rateLimiterService, JsonMapper jsonMapper) {
        this.rateLimiterService = rateLimiterService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        ConsumptionProbe probe;
        if (AUTH_RATE_LIMITED_PATHS.contains(path)) {
            probe = rateLimiterService.consumeAuth("ip:" + clientIp(request));
        } else if (path.startsWith(CONTENT_PATH_PREFIX)) {
            probe = rateLimiterService.consumeContent(contentRateLimitKey(request));
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        if (!probe.isConsumed()) {
            long retryAfterSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);
            rejectTooManyRequests(response, retryAfterSeconds);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String contentRateLimitKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String userId) {
            return "user:" + userId;
        }
        return "ip:" + clientIp(request);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void rejectTooManyRequests(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
                "Too many requests. Please slow down and try again in " + retryAfterSeconds + " second(s).");

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        jsonMapper.writeValue(response.getWriter(), problem);
    }
}