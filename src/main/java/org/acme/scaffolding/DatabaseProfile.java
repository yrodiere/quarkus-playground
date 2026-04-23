package org.acme.scaffolding;

import io.quarkus.runtime.configuration.ConfigUtils;

public enum DatabaseProfile {
    ORACLE("oracle"),
    POSTGRESQL("postgresql"),
    MSSQL("mssql");

    public final String name;

    DatabaseProfile(String name) {
        this.name = name;
    }

    public static DatabaseProfile current() {
        var profiles = ConfigUtils.getProfiles();
        for (DatabaseProfile profile : DatabaseProfile.values()) {
            if ( profiles.contains(profile.name) ) {
                return profile;
            }
        }
        throw new IllegalStateException("No database profile enabled");
    }
}
