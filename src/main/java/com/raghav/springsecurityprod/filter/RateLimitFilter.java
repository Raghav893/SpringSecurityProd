package com.raghav.springsecurityprod.filter;

import com.raghav.springsecurityprod.config.RateLimitConfig;
import com.raghav.springsecurityprod.entity.RateLimitRule;
import com.raghav.springsecurityprod.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitConfig rateLimitConfig;
    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<RateLimitRule> rateLimitRuleOpt = rateLimitConfig.forPath(request.getServletPath());

        if (rateLimitRuleOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        RateLimitRule rule = rateLimitRuleOpt.get();

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request,10*1024);
        String normalizedKey = null;
        if (rule.perKey()){
            normalizedKey = extractNormalizedEmail(wrappedRequest);
            if (normalizedKey == null) {
                filterChain.doFilter(wrappedRequest,response);
                return;
            }
        }

        String ip = wrappedRequest.getRemoteAddr();
        String ipBucketKey = "ratelimit:" + rule.path() + ":ip:" + ip;
        String keyBucketKey = rule.perKey() ? "ratelimit:" + rule.path() + ":key:" + normalizedKey : null;
        boolean ipOk = !rule.perIp() || rateLimitService.wouldPass(ipBucketKey, rule);
        boolean keyOk = !rule.perKey() || rateLimitService.wouldPass(keyBucketKey, rule);
        if (!ipOk || !keyOk) {
            writeTooManyRequests(response);
            return;
        }

        if (rule.perIp()) {
            rateLimitService.consume(ipBucketKey, rule);
        }
        if (rule.perKey()) {
            rateLimitService.consume(keyBucketKey, rule);
        }

        filterChain.doFilter(wrappedRequest, response);

    }
    private String extractNormalizedEmail(ContentCachingRequestWrapper request) {
        try {
            // Must read body via input stream since content isn't cached until after read
            byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
            JsonNode node = objectMapper.readTree(body);
            JsonNode emailNode = node.get("email");
            if (emailNode == null || emailNode.asText().isBlank()) {
                return null;
            }
            return emailNode.asText().trim().toLowerCase();
        } catch (IOException e) {
            return null;
        }
    }
    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }
}
