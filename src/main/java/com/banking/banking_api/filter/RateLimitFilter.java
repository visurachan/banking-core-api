
package com.banking.banking_api.filter;

import com.ratelimiter.rl_service.sdk.RateLimiterClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Order(1)
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterClient rateLimiterClient;

    // endpoints to rate limit
    private static final Set<String> RATE_LIMITED_PATHS = Set.of(
            "/api/v1/account/deposit",
            "/api/v1/account/withdraw"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // check if this path needs rate limiting
        // transfer uses path variable so check with startsWith
        boolean shouldRateLimit = RATE_LIMITED_PATHS.contains(path)
                || path.matches("/api/v1/account/.+/transfer");

        if (!shouldRateLimit) {
            filterChain.doFilter(request, response);
            return;
        }

        // extract JWT
        String jwt = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        // check rate limit
        RateLimiterClient.Result result = rateLimiterClient.check("banking", jwt);

        // always set headers
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.resetAt()));

        if (!result.allowed()) {
            log.warn("Rate limit exceeded — path={}", path);
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"Too Many Requests\", " +
                            "\"retryAfterSeconds\": " + result.retryAfterSeconds() + "}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
