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

# How to get a test report

To get a detailed report after the tests ran:

```shell
./mvnw surefire-report:failsafe-report-only
xdg-open target/reports/failsafe.html
```

In one simple command:

```shell
./mvnw clean verify; ./mvnw surefire-report:failsafe-report-only && xdg-open target/reports/failsafe.html
```

Or, with native:

```shell
./mvnw clean verify; ./mvnw verify -Dnative; ./mvnw surefire-report:failsafe-report-only && xdg-open target/reports/failsafe.html
```
