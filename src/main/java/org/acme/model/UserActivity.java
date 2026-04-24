package org.acme.model;

import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityResult;
import jakarta.persistence.FieldResult;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;

import java.time.LocalDateTime;

@Entity
@SqlResultSetMapping(
        name = ReturnedUser.RESULT_SET_MAPPING,
        classes = @ConstructorResult(
                targetClass = ReturnedUser.class,
                columns = {@ColumnResult(name = "username"), @ColumnResult(name = "fullname")}
        )
)
@SqlResultSetMapping(
        name = UserActivity.RESULT_SET_MAPPING_WITH_PROFILE,
        entities = @EntityResult(
                entityClass = UserActivity.class,
                fields = {
                        @FieldResult(name = "id", column = "id"),
                        @FieldResult(name = "activityTime", column = "activitytime"),
                        @FieldResult(name = "profile.id", column = "profile_id"),
                        @FieldResult(name = "profile.username", column = "username"),
                        @FieldResult(name = "profile.fullName", column = "profile_fullname")
                }
        )
)
public class UserActivity {
    public static final String RESULT_SET_MAPPING_WITH_PROFILE = "UserActivityWithProfile";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username")
    public UserProfile profile;

    public LocalDateTime activityTime;
}
