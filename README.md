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


## RateLimiter pattern implemented on Gateway Server

### run Redis server
docker run -p 6379:6379 --name eazyredis -d redis

### Load test sending requests to Gateway Server -> cards service using ApacheBench
-n: number of requests
-c: (concurrency) number of threads (we are sending 2 concurrent requests each time
-v: verbose

docker run --rm jjasonek/apache-benchmark:s1 -n 10 -c 2 -v 3 http://host.docker.internal:8072/eazybank/cards/api/contact-info

PS C:\Training\Microservices\apache-benchmark-docker> docker run --rm jjasonek/apache-benchmark:s1 -n 10 -c 2 -v 3 http://host.docker.internal:8072/eazybank/cards/api/contact-info
This is ApacheBench, Version 2.3 <$Revision: 1913912 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking host.docker.internal (be patient)...INFO: GET header ==
---
GET /eazybank/cards/api/contact-info HTTP/1.0
Host: host.docker.internal:8072
User-Agent: ApacheBench/2.3
Accept: */*


---
LOG: header received:
HTTP/1.1 200 OK
transfer-encoding: chunked
X-RateLimit-Remaining: 0
X-RateLimit-Requested-Tokens: 1
X-RateLimit-Burst-Capacity: 1
X-RateLimit-Replenish-Rate: 1
Content-Type: application/json
Date: Wed, 25 Jun 2025 20:06:52 GMT
eazybank-correlation-id: 685337c7-b0aa-488d-b90d-0f5efe8ee3b1
X-Response-Time: 2025-06-25T22:02:10.522675800
connection: close


LOG: Response code = 200
LOG: header received:
HTTP/1.0 429 Too Many Requests
X-RateLimit-Remaining: 0
X-RateLimit-Requested-Tokens: 1
X-RateLimit-Burst-Capacity: 1
X-RateLimit-Replenish-Rate: 1
content-length: 0


WARNING: Response code not 2xx (429)
LOG: header received:
HTTP/1.0 429 Too Many Requests
X-RateLimit-Remaining: 0
X-RateLimit-Requested-Tokens: 1
X-RateLimit-Burst-Capacity: 1
X-RateLimit-Replenish-Rate: 1
content-length: 0


WARNING: Response code not 2xx (429)
LOG: header received:
HTTP/1.0 429 Too Many Requests
X-RateLimit-Remaining: 0
X-RateLimit-Requested-Tokens: 1
X-RateLimit-Burst-Capacity: 1
X-RateLimit-Replenish-Rate: 1
content-length: 0


WARNING: Response code not 2xx (429)
LOG: header received:
HTTP/1.0 429 Too Many Requests
X-RateLimit-Remaining: 0
X-RateLimit-Requested-Tokens: 1
X-RateLimit-Burst-Capacity: 1
X-RateLimit-Replenish-Rate: 1
content-length: 0


WARNING: Response code not 2xx (429)
LOG: header received:
HTTP/1.0 429 Too Many Requests
X-RateLimit-Remaining: 0
X-RateLimit-Requested-Tokens: 1
X-RateLimit-Burst-Capacity: 1
X-RateLimit-Replenish-Rate: 1
content-length: 0


WARNING: Response code not 2xx (429)
LOG: header received:
HTTP/1.0 429 Too Many Requests
X-RateLimit-Remaining: 0
X-RateLimit-Requested-Tokens: 1
X-RateLimit-Burst-Capacity: 1
X-RateLimit-Replenish-Rate: 1
content-length: 0


WARNING: Response code not 2xx (429)
LOG: header received:
HTTP/1.0 429 Too Many Requests
X-RateLimit-Remaining: 0
X-RateLimit-Requested-Tokens: 1
X-RateLimit-Burst-Capacity: 1
X-RateLimit-Replenish-Rate: 1
content-length: 0


WARNING: Response code not 2xx (429)
LOG: header received:
HTTP/1.0 429 Too Many Requests
X-RateLimit-Remaining: 0
X-RateLimit-Requested-Tokens: 1
X-RateLimit-Burst-Capacity: 1
X-RateLimit-Replenish-Rate: 1
content-length: 0


WARNING: Response code not 2xx (429)
LOG: header received:
HTTP/1.0 429 Too Many Requests
X-RateLimit-Remaining: 0
X-RateLimit-Requested-Tokens: 1
X-RateLimit-Burst-Capacity: 1
X-RateLimit-Replenish-Rate: 1
content-length: 0


WARNING: Response code not 2xx (429)
..done


Server Software:
Server Hostname:        host.docker.internal
Server Port:            8072

Document Path:          /eazybank/cards/api/contact-info
Document Length:        213 bytes

Concurrency Level:      2
Time taken for tests:   0.435 seconds
Complete requests:      10
Failed requests:        9
(Connect: 0, Receive: 0, Length: 9, Exceptions: 0)
Non-2xx responses:      9
Total transferred:      2146 bytes
HTML transferred:       213 bytes
Requests per second:    23.01 [#/sec] (mean)
Time per request:       86.925 [ms] (mean)
Time per request:       43.462 [ms] (mean, across all concurrent requests)
Transfer rate:          4.82 [Kbytes/sec] received

Connection Times (ms)
min  mean[+/-sd] median   max
Connect:        1    1   0.3      1       2
Processing:     5   46 122.2      8     394
Waiting:        5   45 120.0      8     387
Total:          6   47 122.5     10     396

Percentage of the requests served within a certain time (ms)
50%     10
66%     10
75%     10
80%     10
90%    396
95%    396
98%    396
99%    396
100%    396 (longest request)


## Rate Limiter in accounts microservice

### Normal response
GET http://localhost:8072/eazybank/accounts/api/java-version
response: C:\Java\jdk-21


### After some number of requests (manulaly) throug the postman:
GET http://localhost:8072/eazybank/accounts/api/java-version
{
    "apiPath": "uri=/api/java-version",
    "errorCode": "INTERNAL_SERVER_ERROR",
    "errorMessage": "RateLimiter 'getJavaVersion' does not permit further calls",
    "errorTime": "2025-06-26T14:17:43.785097"
}
