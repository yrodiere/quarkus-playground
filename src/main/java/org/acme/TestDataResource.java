package org.acme;

import jakarta.inject.Inject;
import jakarta.persistence.FindOption;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.hibernate.Session;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

@Path("/test-data")
@Transactional
public class TestDataResource {

    @Inject
    Session session;

    @POST
    @Path("/reset")
    @Produces(MediaType.TEXT_PLAIN)
    public String reset() {
        session.createNativeQuery("DELETE FROM useractivity", Void.class).executeUpdate();
        return "Database reset";
    }

    @POST
    @Path("/add-activity")
    @Produces(MediaType.TEXT_PLAIN)
    public String addActivity(@RestQuery String username) {
        UserActivity activity = new UserActivity();
        activity.profile = session.bySimpleNaturalId(UserProfile.class).load(username);
        activity.activityTime = java.time.LocalDateTime.now();
        session.persist(activity);
        return "Activity added for: " + username;
    }

    @GET
    @Path("/activities")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserActivity> getActivities() {
        return session.createQuery(
            "SELECT a FROM UserActivity a ORDER BY a.activityTime", UserActivity.class)
            .getResultList();
    }
}
