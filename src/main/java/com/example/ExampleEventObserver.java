package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class ExampleEventObserver
{
    @Inject
    EntityManager entityManager;

    public void beforeCompletion(@Observes(during = TransactionPhase.BEFORE_COMPLETION) Fruit exampleEvent) {
        // do some query -> this triggers the warning
        List<Fruit> resultList = entityManager.createNamedQuery("Fruits.findAll", Fruit.class).getResultList();
        System.out.println("done with example event");
    }
}
