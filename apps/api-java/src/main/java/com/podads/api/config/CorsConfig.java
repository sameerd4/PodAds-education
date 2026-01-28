package com.podads.api.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Get allowed origins from environment variable, fallback to localhost for local dev
        String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            // Parse comma-separated origins from environment variable
            for (String origin : allowedOrigins.split(",")) {
                config.addAllowedOrigin(origin.trim());
            }
        } else {
            // Default to localhost for local development
            // Allow both localhost and 127.0.0.1 for Safari compatibility
            // Safari may send 127.0.0.1 as origin even when accessing via localhost
            // NOTE: When setAllowCredentials(true), you MUST use exact origins, not patterns
            config.addAllowedOrigin("http://localhost:5173");
            config.addAllowedOrigin("http://127.0.0.1:5173");
        }
        
        // Allow all HTTP methods including OPTIONS for preflight
        config.addAllowedMethod("*");
        
        // Allow all headers (Safari is strict about this)
        config.addAllowedHeader("*");
        
        // Allow credentials if needed
        config.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);
        
        // Apply CORS configuration to all paths
        source.registerCorsConfiguration("/**", config);
        
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
