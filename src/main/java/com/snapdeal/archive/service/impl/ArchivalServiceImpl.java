/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.snapdeal.archive.dao.ArchivalDbDao;
import com.snapdeal.archive.dao.MasterDbDao;
import com.snapdeal.archive.dao.RelationDao;
import com.snapdeal.archive.entity.ExecutionQuery;
import com.snapdeal.archive.entity.ExecutionQuery.Status;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;
import com.snapdeal.archive.service.ArchivalService;
import com.snapdeal.archive.util.ArchivalUtil;
import com.snapdeal.archive.util.SystemLog;
import com.snapdeal.archive.util.TimeTracker;

/**
 * @version 1.0, 10-Mar-2016
 * @author vishesh
 */
@Service
public class ArchivalServiceImpl implements ArchivalService {

    @Autowired
    private ArchivalDbDao                          archivalDbDao;

    @Autowired
    private MasterDbDao                            masterDbDao;

    @Autowired
    private RelationDao                            relationDao;

    private Map<String, List<Map<String, Object>>> tableResultMap               = new HashMap<>();
    private Map<String, Integer>                   insertedTableResultCountMap  = new HashMap<>();
    private Set<ExecutionQuery>                    failedQueryList              = new HashSet<>();
    private Set<ExecutionQuery>                    permanantFailedQueryList     = new HashSet<>();
    private Set<ExecutionQuery>                    successfulCompletedQueryList = new HashSet<>();
    private long totalArchivedCount =0;

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
        while (start < totalObjects) {
            try {
                start = batchArchivalProcess(baseCriteria, batchSize, tt, rt, start);
                ExecutionQuery fq = getExecutionQueryPOJO(tableName, baseCriteria, start, batchSize,ExecutionQuery.Status.SUCCESSFUL);
                successfulCompletedQueryList.add(fq);
            } catch (Exception e) {
                SystemLog.logException(e.getMessage());
                SystemLog.logMessage("Adding failed batch to list and to the Database with status as FAILED to be executed later");
                ExecutionQuery fq = getExecutionQueryPOJO(tableName, baseCriteria, start, batchSize,ExecutionQuery.Status.FAILED);
                failedQueryList.add(fq);
                relationDao.saveExecutionQuery(fq);
            } finally {
                // clear the table result map for each batch processing..
                tableResultMap.clear();
            }
        }

        SystemLog.logMessage("Trying failed tasks..total failed batch size is : " + failedQueryList.size());
        tryFailedTasks(tt);

        tt.trackTimeInMinutes("********************************************************************\n Total time taken to archive data (total rows = "+totalArchivedCount+") is : ");
        SystemLog.logMessage("**************************************************************************************");

        SystemLog.logMessage("Permanantly failed query list is  : " + permanantFailedQueryList);

    }

    private void tryFailedTasks(TimeTracker tt) throws BusinessException {
        Map<String, RelationTable> tableRelationMap = new HashMap<>();
        for (ExecutionQuery fq : failedQueryList) {
            RelationTable rt = null;
            if (tableRelationMap.get(fq.getTableName()) == null) {
                rt = relationDao.getRelationShipTableByTableName(fq.getTableName(), 0);
                tableRelationMap.put(fq.getTableName(), rt);
            } else {
                rt = tableRelationMap.get(fq.getTableName());
            }
            try {
                batchArchivalProcess(fq.getCriteria(), fq.getBatchSize(), tt, rt, fq.getStart());
            } catch (Exception ex) {
                SystemLog.logException(ex.getMessage());
                SystemLog.logMessage("Adding permanantly failed batch to list with status as PERMANANT_FAILED This will not be executed again..Look for this manually");
                ExecutionQuery permanantlyFailed = getExecutionQueryPOJO(fq.getTableName(), fq.getCriteria(), fq.getStart(), fq.getBatchSize(), ExecutionQuery.Status.PERMANANTLY_FAILED);
                permanantFailedQueryList.add(permanantlyFailed);
                relationDao.saveExecutionQuery(permanantlyFailed);
            } finally {
                // clear the table result map for each batch processing..
                tableResultMap.clear();
            }

        }

    }

    private long batchArchivalProcess(String baseCriteria, Long batchSize, TimeTracker tt, RelationTable rt, long start) throws BusinessException {
        TimeTracker batchTracker = new TimeTracker();
        batchTracker.startTracking();
        String criteria = baseCriteria + " limit " + start + "," + batchSize;
        List<Map<String, Object>> result = masterDbDao.getResult(rt, criteria);
        tableResultMap.put(rt.getTableName(), result);
        pushData(rt, result, baseCriteria);

        Boolean isDataVerified = verifyBasedOnCount(rt, criteria);
        SystemLog.logMessage("The verification status for batch size :" + start + " to :" + batchSize + " is : @@@@ " + isDataVerified);

        start = start + batchSize;

        batchTracker.trackTimeInMinutes("=====================================================================\n Time to archive data of batch size " + batchSize + " is : ");
        SystemLog.logMessage("============================================================================");

        tt.trackTimeInMinutes("********************************************************\n Total time elapsed since process was started is : ");
        SystemLog.logMessage("*********************************************************");

        long totalRecordCount = 0;
        for(Map.Entry<String,Integer> entry : insertedTableResultCountMap.entrySet()){
            if(entry.getValue()!=null){
                totalRecordCount+=entry.getValue();
            }
        }
        SystemLog.logMessage("!!!!!!!!!!!!!!!!! Total Records that are arcchived in this batch = " + totalRecordCount);
        totalArchivedCount+=totalRecordCount;
        
        /* // clear the table result map for each batch processing..
        tableResultMap.clear();*/
        return start;
    }

    private ExecutionQuery getExecutionQueryPOJO(String tableName, String baseCriteria, long start, Long batchSize, Status status) {
        ExecutionQuery fq = new ExecutionQuery();
        fq.setBatchSize(batchSize);
        fq.setCriteria(baseCriteria);
        fq.setStart(start);
        fq.setTableName(tableName);
        fq.setStatus(status);
        fq.setCompleteQuery("select * from " + tableName + " where " + baseCriteria + " limit " + start + "," + batchSize);
        return fq;
    }

    private void archieveData(String tableName, String criteria) throws BusinessException {
        RelationTable rt = relationDao.getRelationShipTableByTableName(tableName, 0);
        List<Map<String, Object>> result = masterDbDao.getResult(rt, criteria);
        tableResultMap.put(rt.getTableName(), result);
        pushData(rt, result, criteria);
    }

    @Override
    public void archieveVerifyAndDeleteData(String tableName, String baseCriteria, Long batchSize) throws BusinessException {
        TimeTracker tt = new TimeTracker();
        tt.startTracking();
        RelationTable rt = relationDao.getRelationShipTableByTableName(tableName, 0);
        Long totalObjects = getCountFromMaster(tableName, baseCriteria);
        long start = 0;
        while (start < totalObjects) {
            TimeTracker batchTracker = new TimeTracker();
            batchTracker.startTracking();
            String criteria = baseCriteria + " limit " + start + "," + batchSize;
            List<Map<String, Object>> result = masterDbDao.getResult(rt, criteria);
            tableResultMap.put(rt.getTableName(), result);
            pushData(rt, result, baseCriteria);
            Boolean isVerified = verifyBasedOnCount(rt, baseCriteria);
            if (isVerified) {
                deleteData(rt, result, criteria);
            }

            start = start + batchSize;

            batchTracker.trackTimeInMinutes("=====================================================================\n Time to archive data of batch size " + batchSize + " is : ");
            SystemLog.logMessage("============================================================================");
            tt.trackTimeInMinutes("********************************************************\n Total time elapsed since process was started is : ");
            SystemLog.logMessage("*********************************************************");
            // clear the table result map for each batch processing..
            tableResultMap.clear();
        }
        tt.trackTimeInMinutes("********************************************************************\n Total time taken to archive data is : ");
        SystemLog.logMessage("**************************************************************************************");

    }

    @Override
    public void deleteMasterData(String tableName, String baseCriteria, Long batchSize) throws BusinessException {
        TimeTracker tt = new TimeTracker();
        tt.startTracking();
        RelationTable rt = relationDao.getRelationShipTableByTableName(tableName, 0);
        Long totalObjects = getCountFromMaster(tableName, baseCriteria);
        long start = 0;
        while (start < totalObjects) {
            TimeTracker batchTracker = new TimeTracker();
            batchTracker.startTracking();
            String criteria = baseCriteria + " limit " + start + "," + batchSize;
            List<Map<String, Object>> result = masterDbDao.getResult(rt, criteria);
            tableResultMap.put(rt.getTableName(), result);
            deleteData(rt, result, criteria);
            start = start + batchSize;
            batchTracker.trackTimeInMinutes("=====================================================================\n Time to delete data of batch size " + batchSize + " is : ");
            SystemLog.logMessage("============================================================================");

            // clear the table result map for each batch processing..
            tableResultMap.clear();
        }
        tt.trackTimeInMinutes("********************************************************************\n Total time taken to Delete data is : ");
        SystemLog.logMessage("**************************************************************************************");
    }

    /*private Boolean test(RelationTable rt,String baseCriteria){
        if(tableResultMap.get(rt.getTableName()).size()==insertedTableResultCountMap.get(rt.getTableName())){
            List<Map<String, Object>> masterResult = tableResultMap.get(rt.getTableName());
            List<Map<String, Object>> archivalResult = masterResult; 
            return verifyBasedOnCount(rt,baseCriteria,masterResult,archivalResult);        
        }
        else{
            return false;
        }
    }*/

    /*private Boolean verifyBasedOnCount(RelationTable rt, String baseCriteria, List<Map<String, Object>> masterResult, List<Map<String, Object>> archivalResult) {
        Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {
            RelationTable nextRelation = iterator.next();
            Set inQuerySet = getInQuerySet(masterResult, nextRelation);
            Set inQueryForArchivalSet = getInQuerySet(archivalResult, nextRelation);
            Long inQueryMasterCountResult = archivalDao.getMasterInQueryCountResult(nextRelation,inQuerySet);
            Long inQueryArchivalCountResult = archivalDao.getArchivalInQueryCountResult(nextRelation,inQueryForArchivalSet);
            if(!inQueryMasterCountResult.equals(inQueryArchivalCountResult)){
                return false;
            }
           return verifyBasedOnCount(nextRelation, baseCriteria,);
        }
        
        return true;
    }*/

    private Boolean verifyBasedOnCount(RelationTable rt, String baseCriteria) {

        SystemLog.logMessage("Calling verifyBasedOnCount() method for : " + rt.getTableName());

        /*Long masterCount  =  getCountFromMaster(rt.getTableName(), baseCriteria);
        Long archivalCount = getArchivalCount(rt.getTableName(),baseCriteria);
        if(!masterCount.equals(archivalCount)){
            return false;
        }
        
        if (rt.getForeignRelations() != null && !rt.getForeignRelations().isEmpty()) {
            for (RelationTable foreignRelation : rt.getForeignRelations()) {
                String criteria = getNestedCriteria(foreignRelation, rt, baseCriteria);
                Boolean isVerified = verifyArchivalCount(foreignRelation, criteria);
                if(!isVerified){
                    return false;
                }
            }
        }
        
        Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {
            RelationTable nextRelation = iterator.next();
            Set inQuerySet = getInQuerySet(tableResultMap.get(rt.getTableName()), nextRelation);
            Long inQueryMasterCountResult = archivalDao.getMasterInQueryCountResult(nextRelation,inQuerySet);
            Long inQueryArchivalCountResult = archivalDao.getArchivalInQueryCountResult(nextRelation,inQuerySet);
            if(!inQueryMasterCountResult.equals(inQueryArchivalCountResult)){
                return false;
            }
           return verifyBasedOnCount(nextRelation, baseCriteria,start,batchSize);
        }
        
        return true;*/

        for (String tableName : tableResultMap.keySet()) {
            int masterDataSize = tableResultMap.get(tableName).size();
            int archivedDataSize = insertedTableResultCountMap.get(tableName);
            if (masterDataSize != archivedDataSize) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Long getArchivalCount(String tableName, String baseCriteria) throws BusinessException {
        Long count = archivalDbDao.getCountFromArchival(tableName, baseCriteria);
        return count;
    }

    @Override
    public boolean verifyArchivedData(String tableName, String criteria, Long batchSize) throws BusinessException {
        Long masterCount = masterDbDao.getCountFromMaster(tableName, criteria);
        Long archivalCount = archivalDbDao.getCountFromArchival(tableName, criteria);
        if (masterCount.equals(archivalCount)) {
            RelationTable rt = relationDao.getRelationShipTableByTableName(tableName, 0);
            return verifyArchivalCount(rt, criteria);
        }
        return false;

    }

    private Boolean verifyArchivalCount(RelationTable rt, String criteria) throws BusinessException {

        Long masterCount = getCountFromMaster(rt.getTableName(), criteria);
        Long archivalCount = getArchivalCount(rt.getTableName(), criteria);
        if (!masterCount.equals(archivalCount)) {
            return false;
        }

        return verifyBasedOnCount(rt, criteria);
    }

    private void deleteData(RelationTable rt, List<Map<String, Object>> result, String criteria) throws BusinessException {

        Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {

            RelationTable nextRelation = iterator.next();
            Set inQuerySet = getInQuerySet(result, nextRelation);
            List<Map<String, Object>> newResult = masterDbDao.getInQueryResult(nextRelation, inQuerySet);
            deleteData(nextRelation, newResult, criteria);
        }
        deleteMasterData(rt, result, criteria, rt.getParentRelation());

    }

    private void deleteData(RelationTable rt, String criteria) throws BusinessException {
        List<Map<String, Object>> result = masterDbDao.getResult(rt, criteria);
        tableResultMap.put(rt.getTableName(), result);
        deleteData(rt, result, criteria);
    }

    private void deleteMasterData(RelationTable rt, List<Map<String, Object>> result, String baseCriteria, RelationTable parentRelation) throws BusinessException {

        if (rt.getIsDeletionAllowed()) {
            masterDbDao.deleteFromMasterData(rt, result);
        }
        if (rt.getForeignRelations() != null && !rt.getForeignRelations().isEmpty()) {
            for (RelationTable foreignRelation : rt.getForeignRelations()) {
                String criteria = getNestedCriteria(foreignRelation, rt, baseCriteria);
                //  criteria = "where " + foreignRelation.getRelationColumn() + " in (select " + foreignRelation.getRelatedToColumnName() + " from "
                //        + foreignRelation.getRelatedToTableName() + " " + baseCriteria + " )";
                deleteData(foreignRelation, criteria);
            }
        }

    }

    private void pushData(RelationTable rt, List<Map<String, Object>> result, String criteria) throws BusinessException {

        SystemLog.logMessage("Calling pushData() method for : " + rt.getTableName());
        insertIntoArchivalDb(rt, result, criteria, rt.getParentRelation());
        Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {

            RelationTable nextRelation = iterator.next();
            Set inQuerySet = getInQuerySet(result, nextRelation);
            List<Map<String, Object>> newResult = masterDbDao.getInQueryResult(nextRelation, inQuerySet);
            tableResultMap.put(nextRelation.getTableName(), newResult);
            SystemLog.logMessage("%%%%%Table enteries are : " + tableResultMap.keySet().toString());
            pushData(nextRelation, newResult, criteria);
        }
    }

    private void insertIntoArchivalDb(RelationTable rt, List<Map<String, Object>> result, String baseCriteria, RelationTable parentRelation) throws BusinessException {

        if (rt.getForeignRelations() != null && !rt.getForeignRelations().isEmpty()) {
            for (RelationTable foreignRelation : rt.getForeignRelations()) {
                String criteria = getNestedCriteria(foreignRelation, rt, baseCriteria);
                //  criteria = "where " + foreignRelation.getRelationColumn() + " in (select " + foreignRelation.getRelatedToColumnName() + " from "
                //        + foreignRelation.getRelatedToTableName() + " " + baseCriteria + " )";
                archieveData(foreignRelation.getTableName(), criteria);
            }
        }
        Integer size = archivalDbDao.insertToArchivalDB(rt, result);
        insertedTableResultCountMap.put(rt.getTableName(), size);
    }

    private String getNestedCriteria(RelationTable foreignRelation, RelationTable parentRelation, String baseCriteria) {
        if (foreignRelation == null || foreignRelation.getRelationColumn() == null) {
            return baseCriteria;
        }

        // THE commented code was used to create nested in queries. 
        /* String criteria = "where " + foreignRelation.getRelationColumn() + " in (select " + foreignRelation.getRelatedToColumnName() + " from "
                + foreignRelation.getRelatedToTableName() + " " + getNestedCriteria(parentRelation, parentRelation.getParentRelation(), baseCriteria) + " )";
        
        return criteria;*/

        // This will create the in query, something like 
        // "where id in ('id1','id2'......)"
        String criteria2 = "where " + foreignRelation.getRelationColumn() + " in ( " + getNestedInQuerySet(foreignRelation, parentRelation) + " )";

        return criteria2;
    }

    private String getNestedInQuerySet(RelationTable foreignRelation, RelationTable parentRelation) {
        List<Map<String, Object>> finalFilteredResult = getFilteredResult(foreignRelation, parentRelation);
        Set set = getInQuerySet(finalFilteredResult, foreignRelation);
        StringBuilder inQueryBuilder = new StringBuilder();
        for (Object obj : set) {
            inQueryBuilder.append("'" + obj + "',");
        }
        String inQuery = inQueryBuilder.toString();
        inQuery = inQuery.substring(0, inQuery.length() - 1);
        return inQuery;
    }

    private List<Map<String, Object>> getFilteredResult(RelationTable foreignRelation, RelationTable parentRelation) {
        if (foreignRelation == null || foreignRelation.getRelationColumn() == null) {
            return tableResultMap.get(foreignRelation.getTableName());
        }

        return filterResult(foreignRelation, getFilteredResult(parentRelation, parentRelation.getParentRelation()));

    }

    @SuppressWarnings("unused")
    private List<Map<String, Object>> filterResult(RelationTable foreignRelation, List<Map<String, Object>> parentFilteredResult) {
        List<Map<String, Object>> childFilteredResult = tableResultMap.get(foreignRelation.getTableName());
        if (childFilteredResult == null || childFilteredResult.isEmpty()) {
            return parentFilteredResult;
        }
        List<Map<String, Object>> filteredList = new ArrayList<>();
        String columnType = foreignRelation.getRelatedToColumnType();
        for (Map<String, Object> resultMap : childFilteredResult) {
            Object ChildValue = resultMap.get(foreignRelation.getRelationColumn());
            Boolean isFound = Boolean.FALSE;
            for (Map<String, Object> parentMap : parentFilteredResult) {
                Object parentValue = parentMap.get(foreignRelation.getRelatedToColumnName());
                if (checkForEquality(ChildValue, parentValue, columnType)) {
                    isFound = Boolean.TRUE;
                    break;
                }
            }
            if (isFound) {
                filteredList.add(resultMap);
            }
        }

        return filteredList;
    }

    private boolean checkForEquality(Object childObject, Object parentObject, String columnType) {
        Boolean isEquals = Boolean.FALSE;
        switch (columnType) {
            case "Integer":
                Integer childVal = (Integer) childObject;
                Integer parentVal = (Integer) parentObject;
                isEquals = childVal.equals(parentVal);
                break;
            case "String":
                String childValStr = (String) childObject;
                String parentValStr = (String) parentObject;
                isEquals = childValStr.equals(parentValStr);
                break;
            case "Long":
                Long childValLong = (Long) childObject;
                Long parentValLong = (Long) parentObject;
                isEquals = childValLong.equals(parentValLong);
                break;
        }
        return isEquals;
    }

    private Set<Object> getInQuerySet(List<Map<String, Object>> result, RelationTable rt) {
        SystemLog.logMessage("Getting in query set for " + rt.getTableName());
        Set<Object> set = ArchivalUtil.getPropertySetForListOfMap(result, rt.getRelatedToColumnName());
        SystemLog.logMessage("Fetched in query set for " + rt.getTableName() + " Total count is : " + set.size());
        return set;
    }

    @Override
    public Long getCountFromMaster(String tableName, String criteria) throws BusinessException {
        Long count = masterDbDao.getCountFromMaster(tableName, criteria);
        return count;

    }

    @Override
    public RelationTable getRelationTableByTableName(String tableName) throws BusinessException {
        return relationDao.getRelationShipTableByTableName(tableName, 0);
    }

}
