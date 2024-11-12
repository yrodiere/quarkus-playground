package io.quarkus.playground;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class System {

    @Id
    @GeneratedValue
    private Long id;

    private String name;


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

 

    @Override
    public String toString() {
        return "Contained{" + "id=" + id + ", name=" + name + '}';
    }
    
    

    
    
    
}
