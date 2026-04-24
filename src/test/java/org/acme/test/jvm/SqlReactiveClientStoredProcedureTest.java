package org.acme.test.jvm;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.test.AbstractStoredProcedureTest;
import org.junit.jupiter.api.Disabled;

@QuarkusTest
public class SqlReactiveClientStoredProcedureTest extends AbstractStoredProcedureTest {

    @Override
    protected String getEndpointRoot() {
        return "/reactive-sp";
    }

    @Override
    @Disabled("Entities and persistence context do not make sense with raw Vert.x Reactive SQL clients")
    public void testCallFunctionReturningEntitiesNoAssociation() {
        super.testCallFunctionReturningEntitiesNoAssociation();
    }

    @Override
    @Disabled("Entities and persistence context do not make sense with raw Vert.x Reactive SQL clients")
    public void testCallFunctionReturningEntitiesWithToOne() {
        super.testCallFunctionReturningEntitiesWithToOne();
    }

    @Override
    @Disabled("Entities and persistence context do not make sense with raw Vert.x Reactive SQL clients")
    public void testCallProcedureWithOutputParamEntitiesNoAssociation() {
        super.testCallProcedureWithOutputParamEntitiesNoAssociation();
    }

    @Override
    @Disabled("Entities and persistence context do not make sense with raw Vert.x Reactive SQL clients")
    public void testCallProcedureWithOutputParamEntitiesWithToOne() {
        super.testCallProcedureWithOutputParamEntitiesWithToOne();
    }
}
