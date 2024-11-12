package io.quarkus.playground;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Module {

    @Id
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "sistema_id")
    private System system;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public System getSystem() {
        return system;
    }

    public void setSystem(System system) {
        this.system = system;
    }


    @Override
    public String toString() {
        return "Containing{" + "id=" + id + '}';
    }
    
    
}
