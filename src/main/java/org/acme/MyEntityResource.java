package org.acme;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.hibernate.Session;

import java.util.UUID;

@Path("/myentity")
@Transactional
public class MyEntityResource {

    @Inject
    Session session;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String create(@QueryParam("field") String field) {
        MyEntity myEntity = new MyEntity();
        myEntity.setField(field);
        session.persist(myEntity);
        return myEntity.getId().toString();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get(@QueryParam("field") String field) {
        MyEntity entity = session.createQuery("select e from MyEntity e where e.field = :field", MyEntity.class)
                .setParameter("field", field)
                .getSingleResult();
        return entity.getId().toString();
    }

    @GET
    @Path("native")
    @Produces(MediaType.TEXT_PLAIN)
    public String nativeGet(@QueryParam("field") String field) {
        return bytesToUuid((byte[]) session.createNativeQuery("select id from myentity e where e.field = :field")
                .setParameter("field", field)
                .getSingleResult())
                .toString();
    }

    private UUID bytesToUuid(byte[] data) {
        // Copied from private constructor java.util.UUID.UUID(byte[])
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);
        return new UUID(msb, lsb);
    }
}