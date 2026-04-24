package org.acme.test.jvm;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.test.AbstractStoredProcedureTest;
import org.junit.jupiter.api.Disabled;

@QuarkusTest
public class SqlJdbcStoredProcedureTest extends AbstractStoredProcedureTest {

    @Override
    protected String getEndpointRoot() {
        return "/jdbc-sp";
    }

    @Override
    @Disabled("Entities and persistence context do not make sense with raw JDBC")
    public void testCallFunctionReturningEntitiesNoAssociation() {
        super.testCallFunctionReturningEntitiesNoAssociation();
    }

    @Override
    @Disabled("Entities and persistence context do not make sense with raw JDBC")
    public void testCallFunctionReturningEntitiesWithToOne() {
        super.testCallFunctionReturningEntitiesWithToOne();
    }

    @Override
    @Disabled("Entities and persistence context do not make sense with raw JDBC")
    public void testCallProcedureWithOutputParamEntitiesNoAssociation() {
        super.testCallProcedureWithOutputParamEntitiesNoAssociation();
    }

    @Override
    @Disabled("Entities and persistence context do not make sense with raw JDBC")
    public void testCallProcedureWithOutputParamEntitiesWithToOne() {
        super.testCallProcedureWithOutputParamEntitiesWithToOne();
    }
}
