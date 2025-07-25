package com.eazybytes.gatewayserver;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

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
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(keyResolver()))
                        )
                        .uri("lb://CARDS")
                ).build();
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(
                        TimeLimiterConfig
                                .custom()
                                .timeoutDuration(Duration.ofSeconds(4))
                                .build()
                ).build()
        );
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(
                1,
                1,
                1
        );
    }

    /**
     * Request with header "user" will fall to one bucket.
     * Request with header "anonymous" will fall to another bucket.
     * This KeyResolver ensures, that every request gets a key for the rate limiter.
     * @return key resolver
     */
    @Bean
    KeyResolver keyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
                .defaultIfEmpty("anonymous");
    }
}
