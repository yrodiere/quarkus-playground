package io.quarkus.playground;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Role {

    @Id
    private Long id;
    
    @ManyToOne
    private Module module;
    
    @ManyToOne
    private System system;
    
    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RolOperation> operaciones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public System getSystem() {
        return system;
    }

    public void setSystem(System system) {
        this.system = system;
    }

    public List<RolOperation> getOperaciones() {
        return operaciones;
    }

    public void setOperaciones(List<RolOperation> operaciones) {
        this.operaciones = operaciones;
    }
    
    

    

}
