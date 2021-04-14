package org.acme;

import org.acme.caller.Caller;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/hello")
public class GreetingResource {

    private static final Caller caller1ObtainedDuringStaticInit;
    private static final Caller caller2ObtainedDuringStaticInit;
    private static final Map<String, Caller> callers = new HashMap<>();

    static {
        caller1ObtainedDuringStaticInit = new Caller("foo1");
        callers.put("foo1", caller1ObtainedDuringStaticInit);
        caller2ObtainedDuringStaticInit = new Caller("foo2");
        callers.put("foo2", caller2ObtainedDuringStaticInit);
    }

    @GET
    @Path("static")
    @Produces(MediaType.TEXT_PLAIN)
    public String _static() {
        return caller1ObtainedDuringStaticInit.call();
    }

    @GET
    @Path("static-ambiguous-callsite")
    @Produces(MediaType.TEXT_PLAIN)
    public String static_ambiguous_callsite(@QueryParam String method) {
        Caller caller = callers.get(method);
        return caller.call();
    }
}