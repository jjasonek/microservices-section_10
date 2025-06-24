# Udemy Course Microservices with Spring Boot, Docker, Kubernetes Section_10
https://www.udemy.com/course/master-microservices-with-spring-docker-kubernetes/
## spring version: 3.4.5
## gatewayservice version: 3.5.0
## Java 21

## Note:
### We start on section 9

## Documentation
After adding the library, the swagger page is accessible through address 
http://localhost:8080/swagger-ui/index.html,
http://localhost:8090/swagger-ui/index.html,
http://localhost:9000/swagger-ui/index.html.

## links

### Eureka Server dashboard
http://localhost:8070/

### Link from Eureka Server to Gateway Server
http://172.24.64.1:8072/actuator/info
{
    "app": {
        "name": "gatewayserver",
        "description": "Eazy Bank Gateway Server Application",
        "version": "1.0.0"
    }
}

### Further links
http://localhost:8072/actuator
http://localhost:8072/actuator/gateway
http://localhost:8072/actuator/gateway/routes

### Example invoking an service:
POST http://localhost:8072/eazybank/accounts/api/create


### Resilience4j links:
http://localhost:8072/actuator:
...
"circuitbreakers-name": {
"href": "http://localhost:8072/actuator/circuitbreakers/{name}",
"templated": true
},
"circuitbreakers": {
"href": "http://localhost:8072/actuator/circuitbreakers",
"templated": false
},
"circuitbreakerevents-name": {
"href": "http://localhost:8072/actuator/circuitbreakerevents/{name}",
"templated": true
},
"circuitbreakerevents": {
"href": "http://localhost:8072/actuator/circuitbreakerevents",
"templated": false
},
...

http://localhost:8072/actuator/circuitbreakers
http://localhost:8072/actuator/circuitbreakerevents?name=accountsCircuitBreaker


### For testing the CircuitBreaker on a client we should watch the clients actuator endpoints:
http://localhost:8080/actuator/circuitbreakers
http://localhost:8080/actuator/circuitbreakerevents

### Beware of caching GET requests.
Do clean your Postman cache.


## Setting timeout globally

### This will cause response after timeout similar to this:
{
    "timestamp": "2025-06-24T15:07:48.466+00:00",
    "path": "/eazybank/loans/api/contact-info",
    **"status": 504,**
    **"error": "Gateway Timeout",**
    "requestId": "6be1e6a2-3",
    "message": "Response took longer than timeout: PT2S",
    "trace": "org.springframework.cloud.gateway.support.TimeoutException: Response took longer than timeout: PT2S\r\n\tSuppressed: The stacktrace has been enhanced by Reactor, refer to additional information below: \nAssembly trace from producer [reactor.core.publisher.MonoError] :\n\treactor.core.publisher.Mono.error(Mono.java:299)\n\torg.springframework.cloud.gateway.filter.NettyRoutingFilter.lambda$filter$6(NettyRoutingFilter.java:195)\r\nError has been observed at the following site(s):\r\n\t*____Mono.error ⇢ at org.springframework.cloud.gateway.filter.NettyRoutingFilter.lambda$filter$6(NettyRoutingFilter.java:195)\r\n\t*____Mono.defer ⇢ at org.springframework.cloud.gateway.filter.NettyRoutingFilter.filter(NettyRoutingFilter.java:194)\r\n\t*__Flux.timeout ⇢ at org.springframework.cloud.gateway.filter.NettyRoutingFilter.filter(NettyRoutingFilter.java:193)\r\nOriginal Stack Trace:\r\n"
}
