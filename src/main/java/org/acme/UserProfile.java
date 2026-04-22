package org.acme;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.NaturalId;

import java.util.List;

@Entity
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NaturalId
    @Column(nullable = false)
    public String username;

    @Column(nullable = false)
    public String fullName;

    @OneToMany(mappedBy = "profile")
    public List<UserActivity> activities;
}
