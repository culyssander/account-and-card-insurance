package com.santander.apigateway.filter;

import com.santander.apigateway.router.RouteValidator;
import com.santander.apigateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    public AuthenticationFilter(JwtUtil jwtUtil, RouteValidator routeValidator) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (routeValidator.isSecured(request)) {
                if (!hasAuthorizationHeader(request)) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                String token = extractToken(request);

                try {
                    jwtUtil.validateToken(token);
                } catch (Exception e) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                String userId = jwtUtil.extractUsername(token);
                exchange = exchange.mutate()
                        .request(r -> r.header("X-User-Id", userId))
                        .build();
            }

            return chain.filter(exchange);
        };
    }

    private boolean hasAuthorizationHeader(ServerHttpRequest request) {
        return request.getHeaders().containsHeader(HttpHeaders.AUTHORIZATION);
    }

    private String extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {
        // propriedades de config do filtro, se precisar (ex: roles exigidas)
    }
}