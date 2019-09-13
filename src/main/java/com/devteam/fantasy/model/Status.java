package com.devteam.fantasy.model;

import com.devteam.fantasy.util.EstadoName;
import com.devteam.fantasy.util.StatusName;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

/**
 * @author ComputerGAP
 *
 */
@Entity
@Table(name = "status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    @Column(length = 60)
    private StatusName status;

    public Status() {
    }

    public Status(StatusName status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatusName getStatus() {
        return status;
    }

    public void setStatus(StatusName status) {
        this.status = status;
    }

	@Override
	public String toString() {
		return "Status [status=" + status + "]";
	}
    
    
}
