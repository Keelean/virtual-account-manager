package com.keelean.accountmanager.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "string-array", typeClass = StringArrayType.class )
public abstract class AbstractVersionedAuditable<U, PK extends Serializable> extends AbstractVersionedPersistable<PK> {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 6219787514778472672L;


    @Column(length = 50, updatable = false)
    @CreatedBy
    private String createdBy;

    @Column(updatable = false)
    @CreatedDate
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdDate;

    @Column(length = 50)
    @LastModifiedBy
    private String lastModifiedBy;


    @Column
    @LastModifiedDate
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime lastModifiedDate;

    public String getCreatedBy() {

        return createdBy;
    }


    public void setCreatedBy(final String createdBy) {

        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {

        return this.createdDate;
    }


    public void setCreatedDate(final LocalDateTime createdDate) {

        this.createdDate = null == createdDate ? null : createdDate;
    }

    public String getLastModifiedBy() {

        return lastModifiedBy;
    }

    public void setLastModifiedBy(final String lastModifiedBy) {

        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {

        return this.lastModifiedDate;
    }

    public void setLastModifiedDate(final LocalDateTime lastModifiedDate) {

        this.lastModifiedDate = null == lastModifiedDate ? null : lastModifiedDate;
    }
}
