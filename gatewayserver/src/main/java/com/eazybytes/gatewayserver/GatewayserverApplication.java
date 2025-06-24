package com.eazybytes.gatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootApplication
public class GatewayserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayserverApplication.class, args);
    }

    @Bean
    public RouteLocator eazyBankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder
                .routes()
                .route(p -> p
                        .path("/eazybank/accounts/**")
                        .filters(f -> f.rewritePath("/eazybank/accounts/(?<segment>.*)", "/${segment}")     // rewrite path
                                       .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())     //add header to reponse
                                       .circuitBreaker(config -> config.setName("accountsCircuitBreaker")
                                               .setFallbackUri("forward:/contactSupport"))
                        )
                        .uri("lb://ACCOUNTS")
                )
                .route(p -> p
                        .path("/eazybank/loans/**")
                        .filters(f -> f.rewritePath("/eazybank/loans/(?<segment>.*)", "/${segment}")
                                       .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                        .retry(retryConfig -> retryConfig.setRetries(3)
                                                                         .setMethods(HttpMethod.GET)    // just for GET methods
                                                .setBackoff(
                                                        Duration.ofMillis(100),     // first backoff
                                                        Duration.ofMillis(1000),    // max backoff
                                                        2,      // multiplier (factor)
                                                        true    // use previous backoff to count next one.
                                                ))
                        )
                        .uri("lb://LOANS")
                )
                .route(p -> p
                        .path("/eazybank/cards/**")
                        .filters(f -> f.rewritePath("/eazybank/cards/(?<segment>.*)", "/${segment}")
                                       .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                        )
                        .uri("lb://CARDS")
                ).build();
    }
}
