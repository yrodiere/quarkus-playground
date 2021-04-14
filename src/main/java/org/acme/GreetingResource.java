package org.acme;

import org.acme.caller.Caller;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @GET
    @Path("static-concurrency")
    @Produces(MediaType.TEXT_PLAIN)
    public String static_concurrency() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                if (i % 2 == 0) {
                    tasks.add(() -> invokeManyTimes(caller1ObtainedDuringStaticInit));
                } else {
                    tasks.add(() -> invokeManyTimes(caller2ObtainedDuringStaticInit));
                }
            }
            List<Future<String>> futures = executor.invokeAll(tasks);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            String errors = futures.stream().map(f -> {
                try {
                    return f.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new IllegalStateException(e);
                }
            })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n\n"));
            if (!errors.isEmpty()) {
                throw new WebApplicationException(String.join("\n\n", errors));
            }
            return "OK";
        } finally {
            executor.shutdownNow();
        }
    }

    private static String invokeManyTimes(Caller caller) {
        try {
            for (int j = 0; j < 100000; j++) {
                caller.call();
            }
            return null;
        } catch (Throwable throwable) {
            StringWriter writer = new StringWriter();
            writer.append("Exception while invoking '").append(String.valueOf(caller))
                    .append("': ")
                    .append(throwable.getMessage());
            throwable.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        }
    }

}