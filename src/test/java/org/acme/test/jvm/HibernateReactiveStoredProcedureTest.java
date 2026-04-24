package org.acme.test.jvm;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.test.AbstractStoredProcedureTest;

@QuarkusTest
public class HibernateReactiveStoredProcedureTest extends AbstractStoredProcedureTest {

    @Override
    protected String getEndpointRoot() {
        return "/reactive-orm-sp";
    }
}
