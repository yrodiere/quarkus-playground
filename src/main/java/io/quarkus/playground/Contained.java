package io.quarkus.playground;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Contained {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private Containing containing;
    
    @ManyToOne
    private Containing2 containing2;

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

    public Containing2 getContaining2() {
        return containing2;
    }

    public void setContaining2(Containing2 containing2) {
        this.containing2 = containing2;
    }

    @Override
    public String toString() {
        return "Contained{" + "id=" + id + ", name=" + name + ", containing2=" + containing2 + '}';
    }
    
    

    
    
    
}
