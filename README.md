# Oracle UCP experiment

This project experiments with using Oracle UCP drivers in Quarkus, a currently unsupported/untested scenario.
Agroal runs in "pool-less" mode, i.e. it wraps Oracle UCP but lets UCP handle the connection pool.

UCP can be configured thanks to `quarkus.datasource.jdbc.additional-jdbc-properties."*"` properties -- see `src/main/resources/application.properties`.

JVM mode appears to work correctly, at least for this simple case.
Native compilation hasn't been tried and is very likely to fail.

How to run tests (this will spin up a local Oracle instance using docker/podman):

```shell
./mvnw clean verify
```

How to run in dev mode (this will spin up a local Oracle instance using docker/podman):

```shell
./mvnw quarkus:dev
```

Then use the various CRUD methods:

Create (returns the entity identifier):

```shell
curl -i -XPOST 'localhost:8080/crud?value=foo'
````

Retrieve:

```shell
curl -i -XGET 'localhost:8080/crud/1'
````

Update:

```shell
curl -i -XPUT 'localhost:8080/crud/1?value=bar'
````

Delete:

```shell
curl -i -XDELETE 'localhost:8080/crud/1'
````

You can also access Agroal metrics -- only some of which are relevant in this setup, since Agroal doesn't handle the connection pool (UCP does):

```shell
curl localhost:9000/q/metrics 2>&1 | grep '^agroal_'
```
