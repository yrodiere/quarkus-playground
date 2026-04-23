package org.acme.model;

public record ReturnedUser(String username, String fullName) {
    public static final String RESULT_SET_MAPPING = "ReturnedUser";
}
