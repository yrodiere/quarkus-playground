package io.quarkus.playground;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;

@Entity
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Containing {

    @Id
    private Long id;

    @OneToMany(mappedBy = "containing", cascade = CascadeType.ALL, orphanRemoval = true)
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
        return "Containing{" + "id=" + id + ", contained=" + contained + '}';
    }
    
    
}
