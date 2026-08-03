package com.santander.apigateway.router;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteValidator {

    public static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/v1/login",
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui.html"
    );

    public boolean isSecured(ServerHttpRequest request) {
        return OPEN_ENDPOINTS.stream()
                .noneMatch(uri -> request.getURI().getPath().contains(uri));
    }
}