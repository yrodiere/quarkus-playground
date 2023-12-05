# Reproducer

How to reproduce:

```shell
./mvnw clean verify -Dnative
```

See how you get this in surefire (JVM) tests:

```
2023-12-05 12:11:03,761 INFO  [org.acm.GreetingResource] (executor-thread-1) Info log for call to GreetingResource#hello
2023-12-05 12:11:03,761 TRACE [org.acm.GreetingResource] (executor-thread-1) Trace log for call to GreetingResource#hello
```

But you don't get the trace log in failsafe (native) tests:

```
2023-12-05 12:12:07,383 INFO  [org.acm.GreetingResource] (executor-thread-1) Info log for call to GreetingResource#hello
```

Same goes if you run the app natively:

```shell
./target/code-with-quarkus-1.0.0-SNAPSHOT-runner
```

Then call the endpoint:

```shell
curl 0.0.0.0:8080/hello
```

The logs lack the TRACE level:

```
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2023-12-05 12:12:35,130 INFO  [io.quarkus] (main) code-with-quarkus 1.0.0-SNAPSHOT native (powered by Quarkus 3.6.0) started in 0.011s. Listening on: http://0.0.0.0:8080
2023-12-05 12:12:35,131 INFO  [io.quarkus] (main) Profile prod activated. 
2023-12-05 12:12:35,131 INFO  [io.quarkus] (main) Installed features: [cdi, resteasy-reactive, smallrye-context-propagation, vertx]
2023-12-05 12:12:45,006 INFO  [org.acm.GreetingResource] (executor-thread-1) Info log for call to GreetingResource#hello
```