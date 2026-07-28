package com.santander.apigateway.filter;

import com.santander.apigateway.exception.UnauthorizedException;
import com.santander.apigateway.router.ValidatorRouter;
import com.santander.apigateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticationFilter
       extends AbstractGatewayFilterFactory<AuthenticationFilter.Config>
{

    @Autowired
    private ValidatorRouter router;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        System.out.println("RRRRRRRRRRR");
        return (((exchange, chain) -> {

            if (router.isSecure.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsHeader(HttpHeaders.AUTHORIZATION)) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }


                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    jwtUtil.validateToken(authHeader);
                    exchange.getRequest().mutate()
                            .header("user-logged", jwtUtil.extractUsername(authHeader))
                            .header("correlationId", UUID.randomUUID().toString());
                } catch (UnauthorizedException e) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }

            System.out.println("ROTA: " + router.isSecure.test(exchange.getRequest()));
            System.out.println("ROTA: " + exchange.getRequest().getURI().getPath());

            return chain.filter(exchange);
        }));
    }

    static  class Config {}
}