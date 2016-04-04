/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public enum QueryType {
        INSERT, DELETE;
    }

    @Column(name = "query_type")
    @Enumerated(EnumType.STRING)
    private QueryType queryType;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status    status;

    @Column(name = "exception_message")
    private String    exceptionMessage;

    @Transient
    private String    criteriaWithoutSpace;

    @Column(name = "created_date")
    private Date      createdDate;

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
        // set the criteriaWithoutSpace(remove all spaces from criteria) to this transient field
        this.criteriaWithoutSpace = this.criteria.replaceAll("\\s", "");
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

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((batchSize == null) ? 0 : batchSize.hashCode());
        result = prime * result + ((criteriaWithoutSpace == null) ? 0 : criteriaWithoutSpace.hashCode());
        result = prime * result + ((queryType == null) ? 0 : queryType.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
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
        if (batchSize == null) {
            if (other.batchSize != null)
                return false;
        } else if (!batchSize.equals(other.batchSize))
            return false;
        if (criteriaWithoutSpace == null) {
            if (other.criteriaWithoutSpace != null)
                return false;
        } else if (!criteriaWithoutSpace.equals(other.criteriaWithoutSpace))
            return false;
        if (queryType != other.queryType)
            return false;
        if (start == null) {
            if (other.start != null)
                return false;
        } else if (!start.equals(other.start))
            return false;
        if (status != other.status)
            return false;
        if (tableName == null) {
            if (other.tableName != null)
                return false;
        } else if (!tableName.equals(other.tableName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ExecutionQuery [id=" + id + ", tableName=" + tableName + ", criteria=" + criteria + ", start=" + start + ", batchSize=" + batchSize + ", completeQuery="
                + completeQuery + ", queryType=" + queryType + ", status=" + status + ", exceptionMessage=" + exceptionMessage + ", criteriaWithoutSpace=" + criteriaWithoutSpace
                + ", createdDate=" + createdDate + "]";
    }

}
