package com.ChatApp.Services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LoginRateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final Set<String> LOGIN_PATHS = Set.of("/loginwithemail", "/admin");

    // Capacity and refill amount now match: 5 tokens, refilled together after 1 full minute
    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String path = request.getRequestURI();
        if (!LOGIN_PATHS.contains(path)) {
            return true;
        }

        String clientKey = getClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientKey, k -> newBucket());

        if (bucket.tryConsume(1)) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            return true;
        } else {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            PrintWriter writer = response.getWriter();
            writer.write("{\"Status\": false, \"Message\": \"Too many login attempts. Please try again after some time.\"}");
            writer.flush();
            return false;
        }
    }

    // NEW: runs after the controller method finishes — checks if login actually succeeded
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

        String path = request.getRequestURI();
        if (!LOGIN_PATHS.contains(path)) {
            return;
        }

        // 200 OK means login succeeded — reset this client's bucket so past failures don't linger
        if (response.getStatus() == HttpServletResponse.SC_OK) {
            String clientKey = getClientIp(request);
            buckets.remove(clientKey); // next request builds a fresh, full bucket
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
