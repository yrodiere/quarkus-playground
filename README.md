# Reproducer

## How to run tests

This will spin up a local DB instances using docker/podman.

```shell
./mvnw clean verify
```

Some tests are going to fail, to demonstrate what cannot be done.

Tests will run against multiple databases.
To test against a single one, pass `-Dpostgresql`, `-Doracle` or `-Dmssql` (only one of those):

```shell
./mvnw clean verify -Doracle
```

To test native:

```shell
./mvnw clean verify -Dnative
```
