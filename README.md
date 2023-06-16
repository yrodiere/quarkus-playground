# Reproducer

How to reproduce:

```shell
./mvnw clean package -Dnative
QUARKUS_DATASOURCE_JDBC_URL=foo ./target/code-with-quarkus-1.0.0-SNAPSHOT-runner
```

Result:

```
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2023-06-16 17:25:22,221 WARN  [io.agr.pool] (main) Datasource '<default>': Driver does not support the provided URL: foo
2023-06-16 17:25:22,223 WARN  [io.agr.pool] (agroal-11) Datasource '<default>': Driver does not support the provided URL: foo
2023-06-16 17:25:22,223 WARN  [org.hib.eng.jdb.env.int.JdbcEnvironmentInitiator] (JPA Startup Thread) HHH000342: Could not obtain connection to query metadata: java.sql.SQLException: Driver does not support the provided URL: foo
        at io.agroal.pool.ConnectionFactory.connectionSetup(ConnectionFactory.java:242)
        at io.agroal.pool.ConnectionFactory.createConnection(ConnectionFactory.java:226)
        at io.agroal.pool.ConnectionPool$CreateConnectionTask.call(ConnectionPool.java:536)
        at io.agroal.pool.ConnectionPool$CreateConnectionTask.call(ConnectionPool.java:517)
        at java.base@17.0.7/java.util.concurrent.FutureTask.run(FutureTask.java:264)
        at io.agroal.pool.util.PriorityScheduledExecutor.beforeExecute(PriorityScheduledExecutor.java:75)
        at java.base@17.0.7/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
        at java.base@17.0.7/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
        at java.base@17.0.7/java.lang.Thread.run(Thread.java:833)
        at org.graalvm.nativeimage.builder/com.oracle.svm.core.thread.PlatformThreads.threadStartRoutine(PlatformThreads.java:775)
        at org.graalvm.nativeimage.builder/com.oracle.svm.core.posix.thread.PosixPlatformThreads.pthreadStartRoutine(PosixPlatformThreads.java:203)

2023-06-16 17:25:22,226 ERROR [io.qua.run.Application] (main) Failed to start application (with profile [prod]): java.lang.IllegalArgumentException: Class java.util.UUID[] is instantiated reflectively but was never registered.Register the class by adding "unsafeAllocated" for the class in reflect-config.json.
        at org.graalvm.nativeimage.builder/com.oracle.svm.core.graal.snippets.SubstrateAllocationSnippets.arrayHubErrorStub(SubstrateAllocationSnippets.java:345)
        at org.hibernate.loader.ast.internal.EntityBatchLoaderArrayParam.prepare(EntityBatchLoaderArrayParam.java:176)
        at org.hibernate.persister.entity.AbstractEntityPersister.prepareLoader(AbstractEntityPersister.java:3425)
        at org.hibernate.persister.entity.AbstractEntityPersister.postInstantiate(AbstractEntityPersister.java:3419)
        at org.hibernate.metamodel.model.domain.internal.MappingMetamodelImpl.finishInitialization(MappingMetamodelImpl.java:204)
        at org.hibernate.internal.SessionFactoryImpl.initializeMappingModel(SessionFactoryImpl.java:320)
        at org.hibernate.internal.SessionFactoryImpl.<init>(SessionFactoryImpl.java:270)
        at io.quarkus.hibernate.orm.runtime.boot.FastBootEntityManagerFactoryBuilder.build(FastBootEntityManagerFactoryBuilder.java:84)
        at io.quarkus.hibernate.orm.runtime.FastBootHibernatePersistenceProvider.createEntityManagerFactory(FastBootHibernatePersistenceProvider.java:74)
        at jakarta.persistence.Persistence.createEntityManagerFactory(Persistence.java:80)
        at jakarta.persistence.Persistence.createEntityManagerFactory(Persistence.java:55)
        at io.quarkus.hibernate.orm.runtime.JPAConfig$LazyPersistenceUnit.get(JPAConfig.java:156)
        at io.quarkus.hibernate.orm.runtime.JPAConfig$1.run(JPAConfig.java:64)
        at java.base@17.0.7/java.lang.Thread.run(Thread.java:833)
        at org.graalvm.nativeimage.builder/com.oracle.svm.core.thread.PlatformThreads.threadStartRoutine(PlatformThreads.java:775)
        at org.graalvm.nativeimage.builder/com.oracle.svm.core.posix.thread.PosixPlatformThreads.pthreadStartRoutine(PosixPlatformThreads.java:203)
```