/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.entity;

import java.io.Serializable;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @version 1.0, 29-Mar-2016
 * @author vishesh
 */
@Entity
@Table(name = "execution_query")
public class ExecutionQuery implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -606866641016323760L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private int               id;

    @Column(name = "table_name")
    private String            tableName;

    @Column(name = "criteria")
    private String            criteria;

    @Column(name = "batch_start")
    private Long              start;

    @Column(name = "batch_size")
    private Long              batchSize;

    @Column(name = "complete_query")
    private String            completeQuery;

    public enum Status {
        SUCCESSFUL, FAILED, PERMANANTLY_FAILED;
    }

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Long batchSize) {
        this.batchSize = batchSize;
    }

    public String getCompleteQuery() {
        return completeQuery;
    }

    public void setCompleteQuery(String completeQuery) {
        this.completeQuery = completeQuery;
    }

    @Override
    public String toString() {
        return "FailedQuery [tableName=" + tableName + ", criteria=" + criteria + ", start=" + start + ", batchSize=" + batchSize + ", completeQuery=" + completeQuery + "]";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((start == null) ? 0 : start.hashCode());
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
        ExecutionQuery other = (ExecutionQuery) obj;
        if (start == null) {
            if (other.start != null)
                return false;
        } else if (!start.equals(other.start))
            return false;
        if (tableName == null) {
            if (other.tableName != null)
                return false;
        } else if (!tableName.equals(other.tableName))
            return false;
        return true;
    }

}
