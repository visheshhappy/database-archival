/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.snapdeal.archive.dao.MasterDbDao;
import com.snapdeal.archive.dao.RelationDao;
import com.snapdeal.archive.entity.ExecutionQuery;
import com.snapdeal.archive.entity.ExecutionQuery.QueryType;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;
import com.snapdeal.archive.util.ArchivalUtil;
import com.snapdeal.archive.util.SystemLog;
import com.snapdeal.archive.util.TimeTracker;

/**
 * @version 1.0, 26-Apr-2016
 * @author vishesh
 */
@Service("archivalServiceDBPaginationStrategy")
public class ArchivalServiceDBPaginationStrategy extends ArchivalServiceImpl {

    @Autowired
    private MasterDbDao masterDbDao;

    @Autowired
    private RelationDao relationDao;

    @Override
    public void archieveMasterData(String tableName, String baseCriteria, Long batchSize) throws BusinessException {

        TimeTracker tt = new TimeTracker();
        tt.startTracking();
        RelationTable rt;
        try {
            rt = relationDao.getRelationShipTableByTableName(tableName, 0);
        } catch (BusinessException e1) {
            throw new BusinessException("Error occurred while fetching relation table for table name : " + tableName, e1);
        }
        Long totalObjects = getCountFromMaster(tableName, baseCriteria);
        long start = 0;
        SystemLog.logMessage("Total " + tableName + " Objects to archive is  : " + totalObjects);

        while (start < totalObjects) {
            try {
                start = batchArchivalProcess(baseCriteria, batchSize, tt, rt, start);
                ExecutionQuery fq = ArchivalUtil.getExecutionQueryPOJO(tableName, baseCriteria, start, batchSize, ExecutionQuery.Status.SUCCESSFUL, null, QueryType.INSERT);
                executionStats.get().getSuccessfulCompletedQueryList().add(fq);
            } catch (Exception e) {
                SystemLog.logException(e.getMessage());
                SystemLog.logMessage("Adding failed batch to list and to the Database with status as FAILED to be executed later");
                ExecutionQuery fq = ArchivalUtil.getExecutionQueryPOJO(tableName, baseCriteria, start, batchSize, ExecutionQuery.Status.FAILED, e.getMessage(), QueryType.INSERT);

                // If it was already in this list.. dont save it to DB
                if (!executionStats.get().getFailedQueryList().contains(fq)) {
                    relationDao.saveExecutionQuery(fq);
                }
                // add to failed list
                executionStats.get().getFailedQueryList().add(fq);
            } finally {
                // clear the table result map for each batch processing..
                //  tableResultMap.clear();
                executionStats.get().getTableResultMap().clear();
            }
        }

        SystemLog.logMessage("Trying failed tasks..total failed batch size is : " + executionStats.get().getFailedQueryList().size());
        tryFailedTasks(tt);

        tt.trackTimeInMinutes("********************************************************************\n Total time taken to archive data (total rows = "
                + executionStats.get().getTotalArchivedCount() + ") is : ");
        SystemLog.logMessage("**************************************************************************************");

        SystemLog.logMessage("Permanantly failed query list is  : " + executionStats.get().getPermanantFailedQueryList());

    }

    private long batchArchivalProcess(String baseCriteria, Long batchSize, TimeTracker tt, RelationTable rt, long start) throws BusinessException {
        TimeTracker batchTracker = new TimeTracker();
        batchTracker.startTracking();
        String criteria = baseCriteria + " limit " + start + "," + batchSize;
        List<Map<String, Object>> result = masterDbDao.getResult(rt, criteria);
        executionStats.get().getTableResultMap().put(rt, result);
        pushData(rt, result, baseCriteria);
        verifyBasedOnCount();
        start = start + batchSize;

        batchTracker.trackTimeInMinutes("=====================================================================\n Time to archive data of batch size " + batchSize + " is : ");
        SystemLog.logMessage("============================================================================");

        tt.trackTimeInMinutes("********************************************************\n Total time elapsed since process was started is : ");
        SystemLog.logMessage("*********************************************************");

        long totalRecordCount = 0;
        for (Map.Entry<String, Integer> entry : executionStats.get().getInsertedTableResultCountMap().entrySet()) {
            if (entry.getValue() != null) {
                totalRecordCount += entry.getValue();
            }
        }
        SystemLog.logMessage("!!!!!!!!!!!!!!!!! Total Records that are arcchived in this batch = " + totalRecordCount);
        executionStats.get().setTotalArchivedCount(executionStats.get().getTotalArchivedCount() + totalRecordCount);

        return start;
    }

}
