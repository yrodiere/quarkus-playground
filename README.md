# Reproducer

How to reproduce:

```shell
./mvnw clean package -Dnative
```

Result:

```

```

How to test a newer version of Hibernate ORM:

```shell
./mvnw clean package -Dnative -Dversion.org.hibernate.orm=6.2.6-SNAPSHOT
```