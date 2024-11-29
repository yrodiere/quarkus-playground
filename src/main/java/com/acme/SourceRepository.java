package com.acme;

import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class SourceRepository {

    @Inject
    @PersistenceUnit(PersistenceUnit.DEFAULT)
    EntityManager entityManager;
}