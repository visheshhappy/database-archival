/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.snapdeal.archive.entity.ExecutionQuery;
import com.snapdeal.archive.entity.RelationTable;

/**
 * This class contains all the stats that are related with Archiving
 * 
 * @version 1.0, 30-Mar-2016
 * @author vishesh
 */
public final class ExecutionStats {

    private Map<RelationTable, List<Map<String, Object>>> tableResultMap;
    private Map<String, Integer>                          insertedTableResultCountMap;
    private Set<ExecutionQuery>                           failedQueryList;
    private Set<ExecutionQuery>                           permanantFailedQueryList;
    private Set<ExecutionQuery>                           successfulCompletedQueryList;
    private Long                                          totalArchivedCount;

    // For is_archived strategy
    private Map<String, Set<Object>>                      tableToPrimaryKeySetMap;

    public ExecutionStats() {
        this.tableResultMap = new HashMap<>();
        this.insertedTableResultCountMap = new HashMap<>();
        this.failedQueryList = new HashSet<>();
        this.permanantFailedQueryList = new HashSet<>();
        this.successfulCompletedQueryList = new HashSet<>();
        this.totalArchivedCount = 0L;
        this.tableToPrimaryKeySetMap = new HashMap<>();
    }

    public Map<RelationTable, List<Map<String, Object>>> getTableResultMap() {
        return tableResultMap;
    }

    public Map<String, Integer> getInsertedTableResultCountMap() {
        return insertedTableResultCountMap;
    }

    public Set<ExecutionQuery> getFailedQueryList() {
        return failedQueryList;
    }

    public Set<ExecutionQuery> getPermanantFailedQueryList() {
        return permanantFailedQueryList;
    }

    public Set<ExecutionQuery> getSuccessfulCompletedQueryList() {
        return successfulCompletedQueryList;
    }

    public Long getTotalArchivedCount() {
        return totalArchivedCount;
    }

    public void setTotalArchivedCount(Long totalArchivedCount) {
        this.totalArchivedCount = totalArchivedCount;
    }

    public Map<String, Set<Object>> getTableToPrimaryKeySetMap() {
        return tableToPrimaryKeySetMap;
    }

}
