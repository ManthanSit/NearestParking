package sg.carpark.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;

@Component
public class SearchRateLimitInterceptor implements HandlerInterceptor {
    private final FixedWindowRateLimiter limiter;

    public SearchRateLimitInterceptor(FixedWindowRateLimiter limiter) { this.limiter = limiter; }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        var decision = limiter.check(request.getRemoteAddr(), Instant.now());
        if (decision.allowed()) return true;
        response.setStatus(429);
        response.setHeader("Retry-After", Long.toString(decision.retryAfterSeconds()));
        response.setContentType("application/json");
        response.getWriter().write("{\"code\":\"search_rate_limited\",\"message\":\"Search limit exceeded\"}");
        return false;
    }
}
