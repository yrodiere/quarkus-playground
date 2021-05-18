package io.quarkus.playground;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Contained {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private Containing containing;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Containing getContaining() {
        return containing;
    }

    public void setContaining(Containing containing) {
        this.containing = containing;
    }
}
