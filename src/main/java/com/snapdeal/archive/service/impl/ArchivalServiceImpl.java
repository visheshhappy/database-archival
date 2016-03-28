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
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.snapdeal.archive.dao.ArchivalDao;
import com.snapdeal.archive.entity.RelationTable;
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
    private ArchivalDao                            archivalDao;

    private Map<String, List<Map<String, Object>>> tableResultMap = new HashMap<>();
    private Map<String,Integer> insertedTableResultCountMap = new HashMap<>();

    @Override
    public void archieveMasterData(String tableName, String baseCriteria, Long batchSize) {
        TimeTracker tt = new TimeTracker();
        tt.startTracking();
        RelationTable rt = archivalDao.getRelationShipTableByTableName(tableName, 0);
        Long totalObjects = getCount(tableName, baseCriteria);
        long start = 0;
        while (start < totalObjects) {
            TimeTracker batchTracker = new TimeTracker();
            batchTracker.startTracking();
            String criteria = baseCriteria + " limit " + start + "," + batchSize;
            List<Map<String, Object>> result = archivalDao.getResult(rt, criteria);
            tableResultMap.put(rt.getTableName(), result);
            pushData(rt, result, baseCriteria);
            
            Boolean isDataVerified = verifyBasedOnCount();
            SystemLog.logMessage("The verification status for batch size :" + start+" to :"+ batchSize + " is : @@@@ "+ isDataVerified);
            
            start = start + batchSize;
            
            
            
            batchTracker.trackTimeInMinutes("=====================================================================\n Time to archive data of batch size " + batchSize + " is : ");
            SystemLog.logMessage("============================================================================");
            
            // clear the table result map for each batch processing..
            tableResultMap.clear();
        }
        tt.trackTimeInMinutes("********************************************************************\n Total time taken to archive data is : ");
        SystemLog.logMessage("**************************************************************************************");
    }

    private void archieveData(String tableName, String criteria) {
        RelationTable rt = archivalDao.getRelationShipTableByTableName(tableName, 0);
        List<Map<String, Object>> result = archivalDao.getResult(rt, criteria);
        tableResultMap.put(rt.getTableName(), result);
        pushData(rt, result, criteria);
    }

    @Override
    public void archieveVerifyAndDeleteData(String tableName, String baseCriteria,Long batchSize) {
        /*archieveMasterData(tableName, baseCriteria, batchSize);
        boolean isVerified = verifyArchivedData(tableName, baseCriteria, batchSize);
        if(isVerified){
            deleteMasterData(tableName, baseCriteria, batchSize);
        }*/
        
        TimeTracker tt = new TimeTracker();
        tt.startTracking();
        RelationTable rt = archivalDao.getRelationShipTableByTableName(tableName, 0);
        Long totalObjects = getCount(tableName, baseCriteria);
        long start = 0;
        while (start < totalObjects) {
            TimeTracker batchTracker = new TimeTracker();
            batchTracker.startTracking();
            String criteria = baseCriteria + " limit " + start + "," + batchSize;
            List<Map<String, Object>> result = archivalDao.getResult(rt, criteria);
            tableResultMap.put(rt.getTableName(), result);
            pushData(rt, result, baseCriteria);
            Boolean isVerified = verifyBasedOnCount();
            if(isVerified){
                deleteData(rt, result, criteria);
            }            
            
            start = start + batchSize;
            
            batchTracker.trackTimeInMinutes("=====================================================================\n Time to archive data of batch size " + batchSize + " is : ");
            SystemLog.logMessage("============================================================================");
            
            // clear the table result map for each batch processing..
            tableResultMap.clear();
        }
        tt.trackTimeInMinutes("********************************************************************\n Total time taken to archive data is : ");
        SystemLog.logMessage("**************************************************************************************");
        
        
    }

    @Override
    public void deleteMasterData(String tableName, String baseCriteria,Long batchSize) {
        TimeTracker tt = new TimeTracker();
        tt.startTracking();
        RelationTable rt = archivalDao.getRelationShipTableByTableName(tableName, 0);
        Long totalObjects = getCount(tableName, baseCriteria);
        long start = 0;
        while (start < totalObjects) {
            TimeTracker batchTracker = new TimeTracker();
            batchTracker.startTracking();
            String criteria = baseCriteria + " limit " + start + "," + batchSize;
            List<Map<String, Object>> result = archivalDao.getResult(rt, criteria);
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

    private Boolean verifyBasedOnCount() {
       for(String tableName : tableResultMap.keySet()){
           int masterDataSize = tableResultMap.get(tableName).size();
           int archivedDataSize = insertedTableResultCountMap.get(tableName);
           if(masterDataSize!=archivedDataSize){
               return Boolean.FALSE;
           }
       }
       return Boolean.TRUE;
    }

    @Override
    public boolean verifyArchivedData(String tableName, String criteria,Long batchSize) {
        Long masterCount = archivalDao.getCountFromMaster(tableName, criteria);
        Long archivalCount = archivalDao.getCountFromArchival(tableName, criteria);
        if(masterCount.equals(archivalCount)){
            RelationTable rt = archivalDao.getRelationShipTableByTableName(tableName, 0);
           return verifyArchivalCount(rt,criteria);
        }
        return false;
        

    }

    private Boolean verifyArchivalCount(RelationTable rt, String criteria) {
        /*Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {

            RelationTable nextRelation = iterator.next();
            Set inQuerySet1 = getInQuerySet(masterResult, nextRelation);
            Set inQuerySet2 = getInQuerySet(archivedResult, nextRelation);
            List masterResultInQuery = archivalDao.getInQueryResult(nextRelation, inQuerySet1);
            List archivedResultInQuery = archivalDao.getInQueryResult(nextRelation, inQuerySet2);
            return verifyCopiedData(nextRelation, masterResultInQuery, archivedResultInQuery);
        }*/

        return true;
    }

    private void deleteData(RelationTable rt, List<Map<String, Object>> result, String criteria) {

        Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {

            RelationTable nextRelation = iterator.next();
            Set inQuerySet = getInQuerySet(result, nextRelation);
            List<Map<String, Object>> newResult = archivalDao.getInQueryResult(nextRelation, inQuerySet);
            deleteData(nextRelation, newResult, criteria);
        }
        deleteMasterData(rt, result, criteria, rt.getParentRelation());

    }
    
    private void deleteData(RelationTable rt, String criteria){
        List<Map<String, Object>> result = archivalDao.getResult(rt, criteria);
        tableResultMap.put(rt.getTableName(), result);
        deleteData(rt, result, criteria);
    }

    private void deleteMasterData(RelationTable rt, List<Map<String, Object>> result, String baseCriteria, RelationTable parentRelation) {

        if (rt.getIsDeletionAllowed()) {
            archivalDao.deleteFromMasterData(rt, result);
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

    private Boolean verifyData(RelationTable rt, String criteria) {
        List masterResult = archivalDao.getResult(rt, criteria);
        List archivedResult = archivalDao.getArchivalResult(rt);

        return verifyCopiedData(rt, masterResult, archivedResult);

    }

    private Boolean verifyCopiedData(RelationTable rt, List masterResult, List archivedResult) {
        Boolean isDataVerified = isDataVerified(masterResult, archivedResult);
        if (!isDataVerified) {
            return isDataVerified;
        }

        Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {

            RelationTable nextRelation = iterator.next();
            Set inQuerySet1 = getInQuerySet(masterResult, nextRelation);
            Set inQuerySet2 = getInQuerySet(archivedResult, nextRelation);
            List masterResultInQuery = archivalDao.getInQueryResult(nextRelation, inQuerySet1);
            List archivedResultInQuery = archivalDao.getInQueryResult(nextRelation, inQuerySet2);
            return verifyCopiedData(nextRelation, masterResultInQuery, archivedResultInQuery);
        }

        return true;

    }

    private Boolean isDataVerified(List masterResult, List archivedResult) {
        // TODO Auto-generated method stub
        return null;
    }

    private void pushData(RelationTable rt, List<Map<String, Object>> result, String criteria) {

        SystemLog.logMessage("Calling pushData() method for : " + rt.getTableName());
        insertIntoArchivalDb(rt, result, criteria, rt.getParentRelation());
        Iterator<RelationTable> iterator = rt.getRelations().iterator();
        while (iterator.hasNext()) {

            RelationTable nextRelation = iterator.next();
            Set inQuerySet = getInQuerySet(result, nextRelation);
            List<Map<String, Object>> newResult = archivalDao.getInQueryResult(nextRelation, inQuerySet);
            tableResultMap.put(nextRelation.getTableName(), newResult);
            SystemLog.logMessage("%%%%%Table enteries are : " + tableResultMap.keySet().toString());
            pushData(nextRelation, newResult, criteria);
        }
    }

    private void insertIntoArchivalDb(RelationTable rt, List<Map<String, Object>> result, String baseCriteria, RelationTable parentRelation) {

        if (rt.getForeignRelations() != null && !rt.getForeignRelations().isEmpty()) {
            for (RelationTable foreignRelation : rt.getForeignRelations()) {
                String criteria = getNestedCriteria(foreignRelation, rt, baseCriteria);
                //  criteria = "where " + foreignRelation.getRelationColumn() + " in (select " + foreignRelation.getRelatedToColumnName() + " from "
                //        + foreignRelation.getRelatedToTableName() + " " + baseCriteria + " )";
                archieveData(foreignRelation.getTableName(), criteria);
            }
        }
        Integer size = archivalDao.insertToArchivalDB(rt, result);
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
    public Long getCount(String tableName, String criteria) {
        Long count = archivalDao.getCountFromMaster(tableName, criteria);
        return count;

    }

    @Override
    public RelationTable getRelationTableByTableName(String tableName) {
        return archivalDao.getRelationShipTableByTableName(tableName, 0);
    }

}
