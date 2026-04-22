package org.acme;

import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SqlResultSetMapping;

import java.time.LocalDateTime;

@Entity
@SqlResultSetMapping(
        name = ReturnedUser.RESULT_SET_MAPPING,
        classes = @ConstructorResult(
                targetClass = ReturnedUser.class,
                columns = {@ColumnResult(name = "username"), @ColumnResult(name = "fullname")}
        )
)
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "username", referencedColumnName = "username")
    public UserProfile profile;

    public LocalDateTime activityTime;
}
