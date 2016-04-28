/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.service.impl;

import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.snapdeal.archive.dao.MasterDbDao;
import com.snapdeal.archive.dao.RelationDao;
import com.snapdeal.archive.dto.ExecutionStats;
import com.snapdeal.archive.entity.ExecutionQuery;
import com.snapdeal.archive.entity.ExecutionQuery.QueryType;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;
import com.snapdeal.archive.service.AbstractArchivalService;
import com.snapdeal.archive.util.ArchivalUtil;
import com.snapdeal.archive.util.SystemLog;
import com.snapdeal.archive.util.TimeTracker;

/**
 * @version 1.0, 21-Apr-2016
 * @author vishesh
 */
@Service("archivalServiceUpdateColumn")
public class ArchivalServiceUpdateColumnStrategy extends AbstractArchivalService {

    @Autowired
    private MasterDbDao                 masterDbDao;

    @Autowired
    private RelationDao                 relationDao;

    private ThreadLocal<ExecutionStats> executionStats = new ThreadLocal<ExecutionStats>() {
                                                           protected ExecutionStats initialValue() {
                                                               return new ExecutionStats();
                                                           }
                                                       };

    @Override
    public void deleteMasterData(String tableName, String criteria, Long batchSize) throws BusinessException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean verifyArchivedData(String tableName, String criteria, Long batchSize) throws BusinessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void archieveVerifyAndDeleteData(String tableName, String criteria, Long batchSize) throws BusinessException {
        throw new UnsupportedOperationException();

    }

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
                start = batchArchivalUpdateProcess(rt, baseCriteria, start, batchSize, tt, rt);

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

        //  SystemLog.logMessage("Trying failed tasks..total failed batch size is : " + executionStats.get().getFailedQueryList().size());
        // tryFailedTasks(tt);

        tt.trackTimeInMinutes("********************************************************************\n Total time taken to archive data (total rows = "
                + executionStats.get().getTotalArchivedCount() + ") is : ");
        SystemLog.logMessage("**************************************************************************************");

        SystemLog.logMessage("Permanantly failed query list is  : " + executionStats.get().getPermanantFailedQueryList());
    }

    private long batchArchivalUpdateProcess(RelationTable rt, String baseCriteria, long start, Long batchSize, TimeTracker tt, RelationTable rt2) {

        TimeTracker batchTracker = new TimeTracker();
        batchTracker.startTracking();
        String criteria = baseCriteria;
        pushTopLevelData(rt, criteria, batchSize);
        start = start + batchSize;

        batchTracker.trackTimeInMinutes("=====================================================================\n Time to archive data of batch size " + batchSize + " is : ");
        SystemLog.logMessage("============================================================================");

        tt.trackTimeInMinutes("********************************************************\n Total time elapsed since process was started is : ");
        SystemLog.logMessage("*********************************************************");
        return start;

    }

    private void pushTopLevelData(RelationTable rt, String criteria, Long limitSize) {
       // Set<Object> result = masterDbDao.getPrimaryKeyResultsToBeArchived(rt, criteria, limitSize);
       // executionStats.get().getTableToPrimaryKeySetMap().put(rt.getTableName(), result);
        masterDbDao.markResultsToBeArchived(rt, criteria, limitSize);
        updateArchivalColumn(rt);
    }

    private void updateArchivalColumn(RelationTable rt) {

        if (rt.getForeignRelations() != null && !rt.getForeignRelations().isEmpty()) {
            for (RelationTable foreignRelation : rt.getForeignRelations()) {
            //    Set<Object> result = masterDbDao.getRelatedPrimaryKeyResultToArchive(foreignRelation);
              //  executionStats.get().getTableToPrimaryKeySetMap().put(foreignRelation.getTableName(), result);
                masterDbDao.markRelatedResultToArchive(foreignRelation/*, executionStats.get().getTableToPrimaryKeySetMap().get(foreignRelation.getRelatedToTableName())*/);
                updateArchivalColumn(foreignRelation);
            }
        }

        Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {
            RelationTable nextRelation = iterator.next();
         //   Set<Object> result = masterDbDao.getRelatedPrimaryKeyResultToArchive(nextRelation);
         //   executionStats.get().getTableToPrimaryKeySetMap().put(nextRelation.getTableName(), result);
            masterDbDao.markRelatedResultToArchive(nextRelation/*, executionStats.get().getTableToPrimaryKeySetMap().get(nextRelation.getRelatedToTableName())*/);
            updateArchivalColumn(nextRelation);
        }
    }

    /* private void alterTable(RelationTable rt){
    String tableName = rt.getTableName();
    String columnName = "is_archived";
    String columnType = "int(1)";
    try{
        archivalDbDao.alterTable(tableName,columnName,columnType,Boolean.TRUE);
        if (rt.getForeignRelations() != null && !rt.getForeignRelations().isEmpty()) {
            for (RelationTable foreignRelation : rt.getForeignRelations()) {
                archivalDbDao.alterTable(foreignRelation.getTableName(),columnName,columnType,Boolean.TRUE);
              alterTable(foreignRelation);
            }
        }
        
        Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {
    
           RelationTable nextRelation = iterator.next();
           archivalDbDao.alterTable(nextRelation.getTableName(),columnName,columnType,Boolean.TRUE);
          alterTable(nextRelation);
        }
    }catch(Exception e){
        SystemLog.logException(e.getMessage());
    }
    }*/

}
