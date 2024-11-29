package com.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class Manager{

    @Inject
    SourceRepository srepo;

    @Inject
    TargetRepository srepo;
}