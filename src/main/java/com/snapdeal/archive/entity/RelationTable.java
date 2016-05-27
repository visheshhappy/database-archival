/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    private static final long serialVersionUID = -8337054305060495328L;

    public enum QueryType {
        JOIN, IN;
    }

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

    /* @Column(name = "level")
    private Integer            level;*/

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

    @Column(name = "audit_fields")
    private String             auditFields;

    @Column(name = "query_type")
    @Enumerated(EnumType.STRING)
    private QueryType          queryType;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "archive_information_id")
    private ArchiveInformation archiveInformation;

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public String getAuditFields() {
        return auditFields;
    }

    public void setAuditFields(String auditFields) {
        this.auditFields = auditFields;
    }

    public Set<String> getAuditFieldSet() {
        if (this.auditFields == null) {
            return new HashSet<>();
        }
        Set<String> auditFieldSet = new HashSet<>();
        StringTokenizer tokenizer = new StringTokenizer(this.auditFields, ",");
        while (tokenizer.hasMoreTokens()) {
            auditFieldSet.add(tokenizer.nextToken());
        }
        return auditFieldSet;
    }

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
    /*
    public Integer getLevel() {
        return level;
    }
    
    public void setLevel(Integer level) {
        this.level = level;
    }*/

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

    public ArchiveInformation getArchiveInformation() {
        return archiveInformation;
    }

    public void setArchiveInformation(ArchiveInformation archiveInformation) {
        this.archiveInformation = archiveInformation;
    }

    @Override
    public String toString() {
        return "RelationTable [id=" + id + ", tableName=" + tableName + ", relatedToTableName=" + relatedToTableName + ", relatedToColumnName=" + relatedToColumnName
                + ", relatedToColumnType=" + relatedToColumnType + ", primaryColumn=" + primaryColumn + ", isDeletionAllowed=" + isDeletionAllowed + ", auditFields=" + auditFields
                + ", queryType=" + queryType + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((primaryColumn == null) ? 0 : primaryColumn.hashCode());
        result = prime * result + ((relatedToColumnName == null) ? 0 : relatedToColumnName.hashCode());
        result = prime * result + ((relatedToColumnType == null) ? 0 : relatedToColumnType.hashCode());
        result = prime * result + ((relatedToTableName == null) ? 0 : relatedToTableName.hashCode());
        result = prime * result + ((relationColumn == null) ? 0 : relationColumn.hashCode());
        result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RelationTable other = (RelationTable) obj;
        if (primaryColumn == null) {
            if (other.primaryColumn != null)
                return false;
        } else if (!primaryColumn.equals(other.primaryColumn))
            return false;
        if (relatedToColumnName == null) {
            if (other.relatedToColumnName != null)
                return false;
        } else if (!relatedToColumnName.equals(other.relatedToColumnName))
            return false;
        if (relatedToColumnType == null) {
            if (other.relatedToColumnType != null)
                return false;
        } else if (!relatedToColumnType.equals(other.relatedToColumnType))
            return false;
        if (relatedToTableName == null) {
            if (other.relatedToTableName != null)
                return false;
        } else if (!relatedToTableName.equals(other.relatedToTableName))
            return false;
        if (relationColumn == null) {
            if (other.relationColumn != null)
                return false;
        } else if (!relationColumn.equals(other.relationColumn))
            return false;
        if (tableName == null) {
            if (other.tableName != null)
                return false;
        } else if (!tableName.equals(other.tableName))
            return false;
        return true;
    }

}
