package org.acme.scaffolding;

import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.Response;

public class Utils {
    public static RuntimeException notSupported(String message) {
        return new ServerErrorException(message,
                Response.status(Response.Status.NOT_IMPLEMENTED.getStatusCode(), message)
                        .entity(message)
                        .build());
    }
}
