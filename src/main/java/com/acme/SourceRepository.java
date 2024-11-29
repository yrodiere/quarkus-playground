package com.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class SourceRepository {

    @Inject
    EntityManager entityManager;
}