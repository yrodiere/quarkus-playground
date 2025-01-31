# Reproducer

## How to run tests

This will spin up a local DB instance using docker/podman.

```shell
./mvnw clean verify
```

## How to run in dev mode

This will spin up a local DB instance using docker/podman.

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
