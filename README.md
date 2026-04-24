# Reproducer

This project demonstrates stored procedure and function usage across multiple databases (PostgreSQL, Oracle, MSSQL) using various Quarkus technologies (Hibernate ORM, Hibernate Reactive, JDBC, Vert.x Reactive SQL Client).

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

## Implementation notes

### Calling simple procedures

Procedures and functions that have no parameters, or only input parameters, and simple (scalar) return values can be called multiple ways:

* In JBDC / Vert.x clients either through an API dedicated to procedure calls (e.g. `prepareCall`), or (when the driver/client/DB support it) through the same APIs that would be used for a `SELECT` statement.
* In Hibernate ORM/Reactive either directly through `createProcedureCall`, or through `createNativeQuery` with a `CALL`/`EXECUTE` clause (or a `SELECT` clause that calls the function, when the driver/client/DB support it).

### Calling more complex procedures

More complex procedures may have more constraints:

* Procedures with OUT parameters require dedicated API.
  Vert.x Reactive SQL Clients (and thus Hibernate Reactive) currently lack such an API.
* Some JDBC drivers have specific behavior:
  * Microsoft SQL Server's procedures can't have `OUT` cursor parameters, only result-set return values.
    As a result, the corresponding result set can only be retrieved by calling the procedure using `executeQuery` and an `EXECUTE` clause.
  * Oracle DB does not support implicit conversion of function-returned cursors in SQL.
    As a result, the corresponding result set can only be retrieved by calling the function using `prepareCall` and a custom-syntax: `{ ? = call my_function() }`, where `?` is an `OUT` parameter.

### Cursors in Hibernate

Stored procedures / functions may be defined in such a way that they return a cursor.

Depending on the database and definition, these may be called in different ways in Hibernate ORM, resulting in different execution:

* When called as a native query, the result set can be consumed in a single step and retrieved as a list (`getResultList()`),
  or scrolled through (`scroll()`), which enables batched reads.
* When called as a procedure call, the result set can only be consumed in a single step and retrieved as a list (`getResultList()`),
  it cannot be scrolled through.

Additional limitations apply:

* Some JDBC drivers may not allow running a query, especially a mutation query, in the same transaction as the one currently scrolling through a result set, making batch processes impractical.
* The benefits of scrolling within a transaction are questionable when this scrolling is extensive: long-running transactions can be a serious performance bottleneck.
  One could prefer designing such batch processes around multiple transactions,
  each processing a "page" of results using [key-based pagination](https://docs.hibernate.org/orm/7.3/introduction/html_single/#key-based-pagination).

### Persistence context synchronization

Calling functions or procedures, like any native SQL execution, requires some precautions when done within the context of a "stateful" `Session` (within a "persistence context", or first-level cache).

If entities were changed in the application before the native call, such changes might only be present in the persistence context, and might not have been flushed to the database.
This could cause SQL executed by a function/procedure to work on out-of-date data and thus return out of date data.
To avoid this, applications should use the `addSynchronizedEntityClass()` calls (Hibernate ORM) or `AffectedEntities` argument (Hibernate Reactive) to instruct Hibernate to flush any such change before calling the function/procedure.

Similarly, if entities were simply loaded from the database into the application before the native call, then changes applied by a function/procedure in the database will not automatically be reflected in these already-loaded entities, unless they are explicitly `refresh`ed.

For these reasons, a best-practice is to ensure function/procedure calls are done at the very start of business methods, before any entity operation on the `Session`. This avoids any of the situations mentioned above.

### Entity mapping completeness and persistence context

When calling functions or procedures returning result sets mapped to entities, the resulting entities are attached to the session, allowing lazy-loading, dirty-tracking, etc.

This implies a few constraints:

1. The result set must include all columns needed to construct the entity instance; missing columns will lead to `org.hibernate.exception.SQLGrammarException: Unable to find column position by name`.
2. Similarly, if using a result set mapping, the application is responsible for including all columns in that result set mapping.
   Failing to do so will result in some data being nulled out, possibly leading to bugs.

#### Entity mapping and associations

When calling functions or procedures returning result sets mapped to entities, associations may require some care:

* By default, all associations are considered uninitialized (to be lazy-loaded).
* To-one associations can be initialized eagerly from the result set returned by the function/procedure, through a custom `@SQLResultSetMapping`: see https://docs.hibernate.org/orm/7.3/userguide/html_single/#sql-entity-named-queries.
* To-many associations cannot be initialized eagerly from the result set returned by the function/procedure. 

These constraints can be avoided with more Hibernate-native ways to retrieve entities -- e.g. JPQL/HQL queries, `Criteria` queries, or `find()`/`findMultiple()` with an entity graph -- where any association can be explicitly configured to be fetched eagerly. Multiple SQL statements may be needed in some cases, but they can generally be batched (no N+1 select).

## Feature Support Matrix

The following table shows which features are supported by each technology across all tested databases (PostgreSQL, Oracle, MSSQL).
All tests produce identical results in both JVM and native modes.

| Feature                                                             | JDBC    | Hibernate ORM | Vert.x Reactive SQL Client                | Hibernate Reactive                        |
|---------------------------------------------------------------------|---------|---------------|-------------------------------------------|-------------------------------------------|
| Procedure without parameters                                        | YES     | YES           | YES                                       | YES                                       |
| Procedure with input parameters                                     | YES     | YES           | YES                                       | YES                                       |
| Function returning basic type (scalar)                              | YES     | YES           | YES                                       | YES                                       |
| Function returning tuples                                           | YES     | YES           | YES (PostgreSQL, MSSQL) / NO (Oracle[^1]) | YES (PostgreSQL, MSSQL) / NO (Oracle[^1]) |
| Function returning entities (no associations)                       | N/A[^2] | YES           | N/A[^2]                                   | YES (PostgreSQL, MSSQL) / NO (Oracle[^1]) |
| Function returning entities (with to-one association)               | N/A[^2] | YES           | N/A[^2]                                   | NO[^3]                                    |
| Procedure with output parameter (basic type)                        | YES     | YES           | NO[^4]                                    | NO[^4]                                    |
| Procedure with output parameter (tuples)                            | YES     | YES           | NO[^4]                                    | NO[^4]                                    |
| Procedure with output parameter (entities, no associations)         | N/A[^2] | YES           | N/A[^2]                                   | NO[^4]                                    |
| Procedure with output parameter (entities, with to-one association) | N/A[^2] | YES           | N/A[^2]                                   | NO[^4]                                    |

[^1]: Reactive APIs have no dedicated support for procedure calls, thus Oracle's cursor-returning functions are not supported. See section "Calling more complex procedures" above.
[^2]: Entities and persistence context do not make sense with raw JDBC/reactive SQL clients; these tests are disabled.
[^3]: Not supported due to a known issue in Hibernate Reactive. See section "Entity mapping and associations" above and https://github.com/hibernate/hibernate-reactive/issues/3616.
[^4]: Reactive APIs have no dedicated support for procedure calls, thus output parameters are not supported. See section "Calling more complex procedures" above.
