# Reproducer

How to test with the built-in version of Hibernate ORM:

```shell
./mvnw clean verify
# OR, native mode:
./mvnw clean verify -Dnative
```

How to test a newer version of Hibernate ORM:

```shell
./mvnw clean verify  -Dversion.org.hibernate.orm=6.2.6-SNAPSHOT
# OR, native mode:
./mvnw clean package -Dnative -Dversion.org.hibernate.orm=6.2.6-SNAPSHOT
```