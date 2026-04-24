package org.acme.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.hibernate.SessionFactory;
import com.fasterxml.jackson.datatype.hibernate7.Hibernate7Module;

@Singleton
public class JacksonCustomizer implements ObjectMapperCustomizer {

    @Inject
    SessionFactory sessionFactory;

    @Override
    public void customize(ObjectMapper mapper) {
        mapper.registerModule(new Hibernate7Module(sessionFactory));
    }
}