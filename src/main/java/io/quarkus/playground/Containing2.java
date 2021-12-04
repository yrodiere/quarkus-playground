package io.quarkus.playground;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Containing2 {

    @Id
    private Long id;

    @OneToMany(mappedBy = "containing2")
    private List<Contained> contained = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Contained> getContained() {
        return contained;
    }

    public void setContained(List<Contained> contained) {
        this.contained = contained;
    }

    @Override
    public String toString() {
        return "Containing{" + "id=" + id + '}';
    }
    
    
}
