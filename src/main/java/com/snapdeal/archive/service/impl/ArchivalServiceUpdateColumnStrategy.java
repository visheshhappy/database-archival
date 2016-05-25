/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.snapdeal.archive.dao.ArchivalDbDao;
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

    @Autowired
    private ArchivalDbDao               archivalDbDao;

    @Value("${archived.criteria}")
    private String         CRITERIA;
    
    @Value("${archived.delete.criteria}")
    private String         DELETE_CRITERIA;

    private ThreadLocal<ExecutionStats> executionStats = new ThreadLocal<ExecutionStats>() {
                                                           protected ExecutionStats initialValue() {
                                                               return new ExecutionStats();
                                                           }
                                                       };

    @Override
    public void deleteMasterData(String tableName, String criteria, Long batchSize,String archiveInfoName) throws BusinessException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean verifyArchivedData(String tableName, String criteria, Long batchSize) throws BusinessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void archieveVerifyAndDeleteData(String tableName, String baseCriteria, Long batchSize,String archiveInfoName) throws BusinessException {

        TimeTracker tt = new TimeTracker();
        tt.startTracking();
        RelationTable rt;
        try {
            rt = relationDao.getRelationTableByArchiveInfoNameAndTableName(archiveInfoName, tableName);
        } catch (BusinessException e1) {
            throw new BusinessException("Error occurred while fetching relation table for table name : " + tableName, e1);
        }
        Long totalObjects = getCountFromMaster(tableName, baseCriteria);
        long start = 0;
        SystemLog.logMessage("Total " + tableName + " Objects to archive is  : " + totalObjects);
        int totalRows =0;
        while (start < totalObjects) {
            try {
                SystemLog.logMessage("---------Starting the Batch for START = " + start +" of total objects  = "+ totalObjects+"--------");
                start = batchArchivalUpdateProcess(rt, baseCriteria, start, batchSize, tt, rt);

                SystemLog.logMessage("Initiating data movement to Archival DB..");
                moveMarkedDataToArchival(rt);
                       
                // Verify that the data has been moved to archival DB. If not verified try moving the data again.
                // This way it will try to move and verify at most three times. 
                // TODO : The max number of retry count should be moved to some properties file

                int i = 0;
                while (i < 3) {
                    if (verifyBasedOnCount(executionStats.get().getTableResultMap())) {
                        SystemLog.logMessage("Verification successful..");
                        SystemLog.logMessage("Deleting marked data from Master DB.");
                        deleteMarkedDataFromMaster(rt);
                        SystemLog.logMessage("Deletion successful");
                        break;
                    } else {
                        SystemLog.logMessage("Verification of data failed.. Trying to move data from master. Try count = " + i);
                        moveMarkedDataToArchival(rt);
                        i++;
                    }
                }

                SystemLog.logMessage("Reseting the archival column for foreign relations..");
                resetForeignRelations(rt,batchSize);
                SystemLog.logMessage("Reseting the archival column for foreign relations completed...!!");
                
                ExecutionQuery fq = ArchivalUtil.getExecutionQueryPOJO(tableName, baseCriteria, start, batchSize, ExecutionQuery.Status.SUCCESSFUL, null, QueryType.INSERT);
                executionStats.get().getSuccessfulCompletedQueryList().add(fq);
                
                int numberOfRowInThisBatch = getTotalRowsArchived();
                totalRows+=numberOfRowInThisBatch;
                SystemLog.logMessage("----------------- Total row archived in this batch = "+numberOfRowInThisBatch);
                
                tt.trackTimeInMinutes("********************************************************\n Total time elapsed since process was started is : ");
                SystemLog.logMessage("*********************************************************");
                
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
        
        SystemLog.logMessage("---------Total Number of rows archived = " + totalRows);

        tt.trackTimeInMinutes("********************************************************************\n Total time taken to archive data (total rows = "
                + totalRows + ") is : ");
        SystemLog.logMessage("**************************************************************************************");

        SystemLog.logMessage("Permanantly failed query list is  : " + executionStats.get().getPermanantFailedQueryList());
    

    }

    private int getTotalRowsArchived() {
       int total =0;
       for(List<Map<String, Object>> value : executionStats.get().getTableResultMap().values()){
           total+=value.size();
       }
        return total;
    }

    private void resetForeignRelations(RelationTable rt, Long batchSize) {
        if(rt.getForeignRelations()!=null){
            Iterator<RelationTable> iterator = rt.getForeignRelations().iterator();
            while(iterator.hasNext()){
                RelationTable foreignRelation = iterator.next();
                markArchivalColumnsInMaster(foreignRelation, CRITERIA, batchSize,Boolean.FALSE);
            }
        }
      
        
    }

    @Override
    public void archieveMasterData(String tableName, String baseCriteria, Long batchSize,String archiveInfoName) throws BusinessException {
        TimeTracker tt = new TimeTracker();
        tt.startTracking();
        RelationTable rt;
        try {
            rt = relationDao.getRelationTableByArchiveInfoNameAndTableName(archiveInfoName, tableName);
        } catch (BusinessException e1) {
            throw new BusinessException("Error occurred while fetching relation table for table name : " + tableName, e1);
        }
        Long totalObjects = getCountFromMaster(tableName, baseCriteria);
        long start = 0;
        SystemLog.logMessage("Total " + tableName + " Objects to archive is  : " + totalObjects);

        while (start < totalObjects) {
            try {
                start = batchArchivalUpdateProcess(rt, baseCriteria, start, batchSize, tt, rt);
                tt.trackTimeInMinutes("********************************************************\n Total time elapsed since process was started is : ");
                SystemLog.logMessage("*********************************************************");
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

    private long batchArchivalUpdateProcess(RelationTable rt, String baseCriteria, long start, Long batchSize, TimeTracker tt, RelationTable rt2) throws BusinessException {

        TimeTracker batchTracker = new TimeTracker();
        batchTracker.startTracking();
        String criteria = baseCriteria;

        // This will set the is_archived column to 1 in master DB in rows which are to be transferred.
        markArchivalColumnsInMaster(rt, criteria, batchSize,Boolean.TRUE);

        start = start + batchSize;
        return start;

    }

    private void deleteMarkedDataFromMaster(RelationTable rt) {
        deleteData(rt);
    }

    private void deleteData(RelationTable rt) {
        Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {
            RelationTable nextRelation = iterator.next();
            deleteData(nextRelation);
        }
        deleteMasterData(rt);
        
    }

    private void deleteMasterData(RelationTable rt) {
        if (rt.getIsDeletionAllowed()) {
            masterDbDao.deleteFromMasterData(rt, DELETE_CRITERIA);
        }
        if (rt.getForeignRelations() != null && !rt.getForeignRelations().isEmpty()) {
            for (RelationTable foreignRelation : rt.getForeignRelations()) {
                deleteData(foreignRelation);
            }
        }
        
    }

    private void moveMarkedDataToArchival(RelationTable rt) throws BusinessException {
        TimeTracker tt = new TimeTracker();
        
        tt.startTracking();
        List<Map<String, Object>> result = masterDbDao.getResult(rt, CRITERIA);
        executionStats.get().getTableResultMap().put(rt, result);
        pushData(rt, result);
        tt.trackTimeInMinutes("Total time taken to move data is  : ");

    }

    private void insertIntoArchivalDb(RelationTable rt, List<Map<String, Object>> result) throws BusinessException {
        if (rt.getForeignRelations() != null && !rt.getForeignRelations().isEmpty()) {
            for (RelationTable foreignRelation : rt.getForeignRelations()) {
                archieveData(foreignRelation);
            }
        }
        archivalDbDao.insertToArchivalDB(rt, result);
    }

    private void archieveData(RelationTable rt) throws BusinessException {
        List<Map<String, Object>> result = masterDbDao.getResult(rt, CRITERIA);
        executionStats.get().getTableResultMap().put(rt, result);
        pushData(rt, result);

    }

    private void pushData(RelationTable rt, List<Map<String, Object>> result) throws BusinessException {

        SystemLog.logMessage("Calling pushData() method for : " + rt.getTableName());
        insertIntoArchivalDb(rt, result);

        Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {

            RelationTable nextRelation = iterator.next();
            List<Map<String, Object>> newResult = masterDbDao.getResult(nextRelation, CRITERIA);
            executionStats.get().getTableResultMap().put(nextRelation, newResult);
            pushData(nextRelation, newResult);
        }
    }

    private void markArchivalColumnsInMaster(RelationTable rt, String criteria, Long limitSize,Boolean mark) {
        // Set<Object> result = masterDbDao.getPrimaryKeyResultsToBeArchived(rt, criteria, limitSize);
        // executionStats.get().getTableToPrimaryKeySetMap().put(rt.getTableName(), result);
        masterDbDao.markResultsToBeArchived(rt, criteria, limitSize,mark);
        updateArchivalColumn(rt,mark);
    }

    private void updateArchivalColumn(RelationTable rt,Boolean mark) {

        if (rt.getForeignRelations() != null && !rt.getForeignRelations().isEmpty()) {
            for (RelationTable foreignRelation : rt.getForeignRelations()) {
                //    Set<Object> result = masterDbDao.getRelatedPrimaryKeyResultToArchive(foreignRelation);
                //  executionStats.get().getTableToPrimaryKeySetMap().put(foreignRelation.getTableName(), result);
                masterDbDao.markRelatedResultToArchive(foreignRelation,mark);
                updateArchivalColumn(foreignRelation,mark);
            }
        }

        Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {
            RelationTable nextRelation = iterator.next();
            //   Set<Object> result = masterDbDao.getRelatedPrimaryKeyResultToArchive(nextRelation);
            //   executionStats.get().getTableToPrimaryKeySetMap().put(nextRelation.getTableName(), result);
            masterDbDao.markRelatedResultToArchive(nextRelation,mark);
            updateArchivalColumn(nextRelation,mark);
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
