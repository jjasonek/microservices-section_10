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


## Retry pattern implemented on Gateway Server

### After setting FAKE error in LoansController we should see the retry pattern in the logs:
2025-06-24T18:48:47.309+02:00  INFO 34800 --- [loans] [nio-8090-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2025-06-24T18:48:47.314+02:00 DEBUG 34800 --- [loans] [nio-8090-exec-1] c.e.loans.controller.LoansController     : Invoked Loans contact-info API
2025-06-24T18:48:47.324+02:00  WARN 34800 --- [loans] [nio-8090-exec-1] .m.m.a.ExceptionHandlerExceptionResolver : Resolved [java.lang.RuntimeException: Loans contact-info API FAKE error]
2025-06-24T18:48:47.444+02:00 DEBUG 34800 --- [loans] [nio-8090-exec-2] c.e.loans.controller.LoansController     : Invoked Loans contact-info API
2025-06-24T18:48:47.446+02:00  WARN 34800 --- [loans] [nio-8090-exec-2] .m.m.a.ExceptionHandlerExceptionResolver : Resolved [java.lang.RuntimeException: Loans contact-info API FAKE error]
2025-06-24T18:48:47.656+02:00 DEBUG 34800 --- [loans] [nio-8090-exec-9] c.e.loans.controller.LoansController     : Invoked Loans contact-info API
2025-06-24T18:48:47.657+02:00  WARN 34800 --- [loans] [nio-8090-exec-9] .m.m.a.ExceptionHandlerExceptionResolver : Resolved [java.lang.RuntimeException: Loans contact-info API FAKE error]
2025-06-24T18:48:48.079+02:00 DEBUG 34800 --- [loans] [nio-8090-exec-3] c.e.loans.controller.LoansController     : Invoked Loans contact-info API
2025-06-24T18:48:48.081+02:00  WARN 34800 --- [loans] [nio-8090-exec-3] .m.m.a.ExceptionHandlerExceptionResolver : Resolved [java.lang.RuntimeException: Loans contact-info API FAKE error]

### And after 750ms we got this response in the Postman:
{
"apiPath": "uri=/api/contact-info",
"errorCode": "INTERNAL_SERVER_ERROR",
"errorMessage": "Loans contact-info API FAKE error",
"errorTime": "2025-06-24T18:54:59.7779627"
}


## Retry pattern implemented on accounts service

### Postman response on 
GET http://localhost:8072/eazybank/accounts/api/build-info
0.9 - static value

### accounts service logs:
2025-06-24T21:18:22.601+02:00 DEBUG 4720 --- [accounts] [nio-8080-exec-1] c.e.a.controller.AccountsController      : getBuildInfo() method invoked
2025-06-24T21:18:22.716+02:00 DEBUG 4720 --- [accounts] [nio-8080-exec-1] c.e.a.controller.AccountsController      : getBuildInfo() method invoked
2025-06-24T21:18:22.934+02:00 DEBUG 4720 --- [accounts] [nio-8080-exec-1] c.e.a.controller.AccountsController      : getBuildInfo() method invoked
2025-06-24T21:18:22.936+02:00 DEBUG 4720 --- [accounts] [nio-8080-exec-1] c.e.a.controller.AccountsController      : getBuildInfoFallback() method invoked

### Note: 
Here, the initial attempt is also count as retry, which is different from the retry pattern on Gateway Server. 

### Now after increasing the waitDuration we get response from circuit breaker because it's default timeout (around 1s) 
### is less than waitDuration: 500.
An error occurred. Please try again after come time or contact support team!!!


### Now after we set default timeout for cuircuit breaker to 4s we get the response from retry fallback method:
GET http://localhost:8072/eazybank/accounts/api/build-info
0.9 - static value


### After setting NullPointerException in ignoredExceptions:
GET http://localhost:8072/eazybank/accounts/api/build-info
0.9 - static value

and in log:
2025-06-24T22:26:38.114+02:00 DEBUG 25680 --- [accounts] [nio-8080-exec-1] c.e.a.controller.AccountsController      : getBuildInfo() method invoked
2025-06-24T22:26:38.117+02:00 DEBUG 25680 --- [accounts] [nio-8080-exec-1] c.e.a.controller.AccountsController      : getBuildInfoFallback() method invoked

### After setting TimeoutException in retryExceptions and letting the getBuildInfo() method throw TimeoutException:
GET http://localhost:8072/eazybank/accounts/api/build-info
0.9 - static value

and log:
2025-06-24T22:45:25.438+02:00 DEBUG 26412 --- [accounts] [nio-8080-exec-1] c.e.a.controller.AccountsController      : getBuildInfo() method invoked
2025-06-24T22:45:25.953+02:00 DEBUG 26412 --- [accounts] [nio-8080-exec-1] c.e.a.controller.AccountsController      : getBuildInfo() method invoked
2025-06-24T22:45:26.973+02:00 DEBUG 26412 --- [accounts] [nio-8080-exec-1] c.e.a.controller.AccountsController      : getBuildInfo() method invoked
2025-06-24T22:45:26.976+02:00 DEBUG 26412 --- [accounts] [nio-8080-exec-1] c.e.a.controller.AccountsController      : getBuildInfoFallback() method invoked
