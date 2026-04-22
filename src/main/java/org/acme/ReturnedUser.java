package org.acme;

public record ReturnedUser(String username, String fullName) {
    public static final String RESULT_SET_MAPPING = "ReturnedUser";
}
