package org.acme.scaffolding;

import io.quarkus.arc.ClientProxy;
import io.quarkus.arc.InjectableInstance;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.reactive.datasource.ReactiveDataSource;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Produces;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

// TEST SETUP
// This is only present to allow testing against multiple RDBMS.
// Normal applications would not need this.
// See https://quarkus.io/guides/hibernate-orm#persistence-unit-active
@ApplicationScoped
public class DatabaseProfileProducer {

    @Produces
    @ApplicationScoped
    public DataSource produceDataSource(@Any InjectableInstance<DataSource> dataSources) {
        return firstActive(dataSources, io.quarkus.agroal.DataSource.DataSourceLiteral::new);
    }

    @Produces
    @ApplicationScoped
    public Pool producePool(@Any InjectableInstance<Pool> pools) {
        return firstActive(pools, ReactiveDataSource.ReactiveDataSourceLiteral::new);
    }

    @Produces
    @ApplicationScoped
    public SessionFactory produceSessionFactory(@Any InjectableInstance<SessionFactory> sessionFactories) {
        return firstActive(sessionFactories, PersistenceUnit.PersistenceUnitLiteral::new);
    }

    @Produces
    @ApplicationScoped
    public Session produceSession(@Any InjectableInstance<Session> sessions) {
        return firstActive(sessions, PersistenceUnit.PersistenceUnitLiteral::new);
    }

    @Produces
    @ApplicationScoped
    public Mutiny.SessionFactory produceReactiveSessionFactory(@Any InjectableInstance<Mutiny.SessionFactory> sessionFactories) {
        return firstActive(sessionFactories, PersistenceUnit.PersistenceUnitLiteral::new);
    }

    private <T> T firstActive(InjectableInstance<T> instances, Function<String, Annotation> qualifierType) {
        return Stream.of(DatabaseProfile.values())
                .map(profile -> instances.select(qualifierType.apply(profile.name)))
                .filter(instance -> instance.getHandle().getBean().isActive())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find any active instance among "
                        + instances.handlesStream().map(InstanceHandle::getBean).toList()
                        + ". Active profiles: " + ConfigUtils.getProfiles()))
                .get();
    }

    public static String getDelegateName(Object injectedInstance) {
        Object delegate = ClientProxy.unwrap(injectedInstance);
        var bean = ((ClientProxy) delegate).arc_bean();
        return bean.getQualifiers().stream()
                .map(a -> {
                    if (a.annotationType().equals(PersistenceUnit.class)) {
                        return ((PersistenceUnit)a).value();
                    }
                    else
                    if (a.annotationType().equals(ReactiveDataSource.class)) {
                        return ((ReactiveDataSource)a).value();
                    }
                    else
                    if (a.annotationType().equals(io.quarkus.agroal.DataSource.class)) {
                        return ((io.quarkus.agroal.DataSource)a).value();
                    }
                    else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot infer name of " + injectedInstance));
    }
}

