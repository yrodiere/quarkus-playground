package org.acme;

import org.acme.caller.Caller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    private static final Caller callerObtainedDuringStaticInit;

    static {
        callerObtainedDuringStaticInit = new Caller("foo1");
    }

    @GET
    @Path("static")
    @Produces(MediaType.TEXT_PLAIN)
    public String _static() {
        return callerObtainedDuringStaticInit.call();
    }

    @GET
    @Path("runtime")
    @Produces(MediaType.TEXT_PLAIN)
    public String runtime() {
        return new Caller("foo2").call();
    }

    @GET
    @Path("static-runtime")
    @Produces(MediaType.TEXT_PLAIN)
    public String static_runtime() {
        // Use reflection on a method that was already obtained through reflection during static init
        return new Caller("foo1").call();
    }
}