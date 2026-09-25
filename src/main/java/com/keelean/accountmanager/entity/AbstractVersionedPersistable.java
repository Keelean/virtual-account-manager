package com.keelean.accountmanager.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.io.Serializable;

@MappedSuperclass
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"new","businessId","manageable"})
public abstract class AbstractVersionedPersistable<PK extends Serializable> extends AbstractPersistable<PK> {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = -5740040475579523635L;

    @Version
    @Column(name = "opt_lock")
    private long optlock;

    public long getOptlock() {
        return optlock;
    }

    public void setOptlock(long optlock) {
        this.optlock = optlock;
    }

    @Override
    public void setId(PK id) {
        super.setId(id);
    }


    public String getBusinessId() {
        return getId().toString();
    }

    @Override
    public String toString() {
        return String.format("%s(%s,%s)", this.getClass().getName(), getId(), optlock);
    }
}
