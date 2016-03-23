/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @version 1.0, 10-Mar-2016
 * @author vishesh
 */

@Entity
@Table(name = "relation_table")
public class RelationTable implements Serializable {

    /**
     * 
     */
    private static final long  serialVersionUID = -8337054305060495328L;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long               id;

    @Column(name = "table_name")
    private String             tableName;
    @Column(name = "related_table_name")
    private String             relatedToTableName;

    @Column(name = "related_column_name")
    private String             relatedToColumnName;

    @Column(name = "level")
    private Integer            level;

    @Column(name = "relation_column")
    private String             relationColumn;

    @Column(name = "related_column_type")
    private String             relatedToColumnType;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "parent_relation_id")
    private RelationTable      parentRelation;

    @OneToMany(mappedBy = "parentRelation", fetch = FetchType.EAGER)
    private Set<RelationTable> relations        = new HashSet<RelationTable>();

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "foreign_relation_id")
    private RelationTable      foreignRelation;

    @OneToMany(mappedBy = "foreignRelation", fetch = FetchType.EAGER)
    private Set<RelationTable> foreignRelations = new HashSet<RelationTable>();

    @Column(name = "primary_column")
    private String             primaryColumn;

    @Column(name = "deletion_allowed")
    private Boolean            isDeletionAllowed;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRelatedToTableName() {
        return relatedToTableName;
    }

    public void setRelatedToTableName(String relatedToTableName) {
        this.relatedToTableName = relatedToTableName;
    }

    public String getRelatedToColumnName() {
        return relatedToColumnName;
    }

    public void setRelatedToColumnName(String relatedToColumnName) {
        this.relatedToColumnName = relatedToColumnName;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getRelatedToColumnType() {
        return relatedToColumnType;
    }

    public void setRelatedToColumnType(String relatedToColumnType) {
        this.relatedToColumnType = relatedToColumnType;
    }

    public Set<RelationTable> getRelations() {
        return relations;
    }

    public void setRelations(Set<RelationTable> relations) {
        this.relations = relations;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRelationColumn() {
        return relationColumn;
    }

    public void setRelationColumn(String relationColumn) {
        this.relationColumn = relationColumn;
    }

    public RelationTable getParentRelation() {
        return parentRelation;
    }

    public void setParentRelation(RelationTable parentRelation) {
        this.parentRelation = parentRelation;
    }

    public RelationTable getForeignRelation() {
        return foreignRelation;
    }

    public void setForeignRelation(RelationTable foreignRelation) {
        this.foreignRelation = foreignRelation;
    }

    public Set<RelationTable> getForeignRelations() {
        return foreignRelations;
    }

    public void setForeignRelations(Set<RelationTable> foreignRelations) {
        this.foreignRelations = foreignRelations;
    }

    public String getPrimaryColumn() {
        return primaryColumn;
    }

    public void setPrimaryColumn(String primaryColumn) {
        this.primaryColumn = primaryColumn;
    }

    public Boolean getIsDeletionAllowed() {
        return isDeletionAllowed;
    }

    public void setIsDeletionAllowed(Boolean isDeletionAllowed) {
        this.isDeletionAllowed = isDeletionAllowed;
    }

    @Override
    public String toString() {
        return "RelationTable [id=" + id + ", tableName=" + tableName + ", relatedToTableName=" + relatedToTableName + ", relatedToColumnName=" + relatedToColumnName + ", level="
                + level + ", relationColumn=" + relationColumn + ", relatedToColumnType=" + relatedToColumnType + ", parentRelation=" + parentRelation + ", relations=" + relations
                + "]";
    }

}
