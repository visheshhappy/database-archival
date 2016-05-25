/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @version 1.0, 24-May-2016
 * @author vishesh
 */
@Entity
@Table(name = "archive_information")
public class ArchiveInformation implements Serializable {

    /**
     * 
     */
    private static final long  serialVersionUID = 7597901932513844051L;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long               id;

    @Column(name = "name")
    private String             name;

    @Column(name = "description")
    private String             description;

    @OneToMany(mappedBy = "archiveInformation", fetch = FetchType.EAGER)
    private Set<RelationTable> relations;

    @OneToMany(mappedBy = "archiveInformation", fetch = FetchType.EAGER)
    private Set<DatabaseEntry> databaseEntries;

    public Set<DatabaseEntry> getDatabaseEntries() {
        return databaseEntries;
    }

    public void setDatabaseEntries(Set<DatabaseEntry> databaseEntries) {
        this.databaseEntries = databaseEntries;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<RelationTable> getRelations() {
        return relations;
    }

    public void setRelations(Set<RelationTable> relations) {
        this.relations = relations;
    }

    @Override
    public String toString() {
        return "ArchiveInformation [id=" + id + ", name=" + name + ", description=" + description + ", relations=" + relations + "]";
    }

}
