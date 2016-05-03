/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.snapdeal.archive.dao.MasterDbDao;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.entity.RelationTable.QueryType;
import com.snapdeal.archive.exception.BusinessException;
import com.snapdeal.archive.util.SystemLog;
import com.snapdeal.archive.util.TimeTracker;

/**
 * @version 1.0, 29-Mar-2016
 * @author vishesh
 */
@Service
public class MasterDbDaoImpl implements MasterDbDao {

    @Autowired
    private SimpleJdbcTemplate simpleJdbcTemplate;
    
    @Autowired
    private SimpleJdbcTemplate archivalJdbcTemplate;

    @Override
    @Transactional("masterTransactionManager")
    public List<Map<String, Object>> getResult(RelationTable rt, String criteria) throws BusinessException {
        String query = "select * from " + rt.getTableName() + " " + criteria;
        SystemLog.logMessage("Getting Result for query.!!! \n " + query);
        TimeTracker tt = new TimeTracker();
        tt.startTracking();
        Object[] params = null;
        try {
            List<Map<String, Object>> result = simpleJdbcTemplate.queryForList(query, params);
            tt.trackTimeInSeconds("@@@@@@@Total time taken to execute query is  : ");
            return result;
        } catch (Exception e) {
            SystemLog.logMessage(e.getMessage());
            throw new BusinessException(e);
        }
    }

    @Override
    @Transactional("masterTransactionManager")
    public List<Map<String, Object>> getInQueryResult(RelationTable rt, Set inQuerySet) throws BusinessException {
        Map<String, Object> queryParams = new HashMap<String, Object>();
        
        try {
            String query = "select * from " + rt.getTableName() + " where " + rt.getRelationColumn() + " IN (:inQuerySet)";
            queryParams.put("inQuerySet", inQuerySet);
            TimeTracker tt = new TimeTracker();
            tt.startTracking();
            SystemLog.logMessage("Getting in query result from db for table : temp_" + rt.getTableName() + " and in query set size is : " + inQuerySet.size());

            List<Map<String, Object>> result = simpleJdbcTemplate.queryForList(query, queryParams);
            tt.trackTimeInSeconds("#######Total time taken to execute 'in' query is : ");
            return result;
        } catch (Exception e) {
            SystemLog.logMessage(e.getMessage());
            throw new BusinessException(e);
        }

    }

    @Override
    @Transactional("archivalTransactionManager") // change this to masterTransactionalManager once the deletion is fully tested and ready for production
    public void deleteFromMasterData(RelationTable rt, List<Map<String, Object>> result) throws BusinessException {
        if (result.isEmpty()) {
            SystemLog.logMessage("No data found to delete..!!");
            return;
        }

        StringBuilder inClauseBuilder = new StringBuilder();

        List<Object[]> batchArgs = new ArrayList<>();
        List<Object> idsList = new ArrayList<>();
        boolean isFirst = true;
        for (Map<String, Object> resultMap : result) {
            // since we know the pimary column of this relation, we therefore are sure that rt.getPrimaryColumn will be in result map
            Object idValue = resultMap.get(rt.getPrimaryColumn());
            if (!isFirst) {
                inClauseBuilder.append(",");
            }
            isFirst = false;
            inClauseBuilder.append("?");
            Object[] obj = { idValue };

            batchArgs.add(obj);
            idsList.add(idValue);
        }

        String inClause = inClauseBuilder.toString();

        String query = "DELETE FROM " + rt.getTableName() + " where " + rt.getPrimaryColumn() + " in ( " + inClause + " )";
        TimeTracker tt = new TimeTracker();
        tt.startTracking();
        try {
            int updateResult = archivalJdbcTemplate.update(query, idsList.toArray());
            SystemLog.logMessage("result after deleting is : " + updateResult);
            tt.trackTimeInMinutes("Time taken for deleting " + rt.getTableName() + " records is : ");
        } catch (Exception e) {
            SystemLog.logMessage(e.getMessage());
            throw new BusinessException(e);
        }

    }

    @Override
    @Transactional("masterTransactionManager")
    public Long getCountFromMaster(String tableName, String criteria) throws BusinessException {
        String query = "select count(*) from " + tableName + " " + criteria;
        SystemLog.logMessage("Getting Result for query.!!! \n " + query);
        TimeTracker tt = new TimeTracker();
        tt.startTracking();
        Object[] params = null;
        try {
            List<Map<String, Object>> count = simpleJdbcTemplate.queryForList(query, params);
            tt.trackTimeInSeconds("@@@@@@@Total time taken to execute query is  : ");
            for (Entry<String, Object> entry : count.get(0).entrySet()) {
                return (Long) entry.getValue();
            }
        } catch (Exception e) {
            SystemLog.logMessage(e.getMessage());
            throw new BusinessException(e);
        }
        return 0L;

    }

    @Override
    @Transactional("masterTransactionManager")
    public Long getMasterInQueryCountResult(RelationTable rt, Set inQuerySet) throws BusinessException {
        try {
            Map<String, Object> queryParams = new HashMap<String, Object>();
            String query = "select count(*) from " + rt.getTableName() + " where " + rt.getRelationColumn() + " IN (:inQuerySet)";
            queryParams.put("inQuerySet", inQuerySet);
            TimeTracker tt = new TimeTracker();
            tt.startTracking();
            SystemLog.logMessage("Getting in query result from db for table : temp_" + rt.getTableName() + " and in query set size is : " + inQuerySet.size());

            List<Map<String, Object>> result = simpleJdbcTemplate.queryForList(query, queryParams);
            tt.trackTimeInSeconds("#######Total time taken to execute 'in' query is : ");
            for (Entry<String, Object> entry : result.get(0).entrySet()) {
                return (Long) entry.getValue();
            }
        } catch (Exception e) {
            throw new BusinessException(e);
        }
        return 0L;
    }

    @Override
    @Transactional("masterTransactionManager") // change this to masterTransactionalManager once the it is fully tested and ready for production
    public void markResultsToBeArchived(RelationTable rt,String criteria,Long limitSize,Boolean mark) {
      TimeTracker tt= new TimeTracker();
      tt.startTracking();
      String query;
      List<Object[]> batchArgs = new ArrayList<>();
      Object[] objArr = new Object[1];
      if(mark){
          query = "update "+rt.getTableName()+" set is_archived=? "+criteria+" and (is_archived is null or is_archived =0) limit "+limitSize;
          objArr[0]=1;
      }else{
          query = "update "+rt.getTableName()+" set is_archived=? where is_archived =1";
          objArr[0]=0;
      }
      batchArgs.add(objArr);
      
      SystemLog.logMessage("Query to be executed is : "+ query);
      int[] result =  simpleJdbcTemplate.batchUpdate(query, batchArgs);
   
      SystemLog.logMessage("result size = " + result.length);
      tt.trackTimeInSeconds("Time taken to update table : "+ rt.getTableName() +" is : ");
        
    }

    @Override
    @Transactional("masterTransactionManager") // change this to masterTransactionalManager once the it is fully tested and ready for production
    public void markRelatedResultToArchive(RelationTable rt,Boolean mark) {
        
        TimeTracker tt= new TimeTracker();
        tt.startTracking();
      
        // Get audit fields. The value of these field needs to be preserved
        String auditFieldClause = getAuditFieldClause(rt);
        
        String query;
        List<Object[]> batchArgs = new ArrayList<>();
        Object[] objArr = new Object[1];
        if(mark){
            if(QueryType.IN.equals(rt.getQueryType())){
                query = "update "+rt.getTableName()+" set is_archived=? "+auditFieldClause+" where "+ rt.getRelationColumn() +" in "
                        + "(select distinct "+rt.getRelatedToColumnName()+" from "+rt.getRelatedToTableName()+" where is_archived=1)"
                        + "and is_archived =0 or is_archived is null";
            }else{
                query = "update "+rt.getTableName()+" t1 inner join "+rt.getRelatedToTableName()+" t2 set t1.is_archived=? "+auditFieldClause+" where t1."+rt.getRelationColumn()+"=t2."+rt.getRelatedToColumnName()+" and "
                        + "t2.is_archived=1 and (t1.is_archived =0 or t1.is_archived is null) ";
            }
            objArr[0]=1;
        }else{
            query = "update "+rt.getTableName()+" set is_archived=? where is_archived =1";
            objArr[0]=0;
        }
        SystemLog.logMessage("Query to be executed is : "+ query);
        batchArgs.add(objArr);
        int[] result =  simpleJdbcTemplate.batchUpdate(query, batchArgs);
        SystemLog.logMessage("result size = " + result.length);
        tt.trackTimeInSeconds("Time taken to update table : "+ rt.getTableName() +" is : ");
        
    }

   /* private String getParentRelationPrimaryKeyNotInQuery(RelationTable parentRelation, Set<Object> primaryKeyNotInSet, String tableName) {
        StringBuilder builder = new StringBuilder();
        builder.append(tableName).append(".").append(parentRelation.getPrimaryColumn()).append(" not in (");
        
        StringBuilder notInQuery = new StringBuilder();
        for(Object obj : primaryKeyNotInSet){
            notInQuery.append("'"+obj.toString()+"',");
        }
        String notInQr = notInQuery.toString();
        notInQr = notInQr.substring(0, notInQr.length()-1);
        
        builder.append(notInQr).append(")");
        return builder.toString();
        
    }*/

    private String getAuditFieldClause(RelationTable rt) {
        
        Set<String> auditFields = rt.getAuditFieldSet();
        // No audit Field clause if the set is empty
        if(auditFields.isEmpty()){
            return "";
        }
        
        StringBuilder auditFieldBuilder = new StringBuilder();
        auditFieldBuilder.append(", ");
        for(String auditField : auditFields){
            auditFieldBuilder.append("t1.").append(auditField).append("=").append("t1.").append(auditField).append(" , ");
        }
        
        String auditFieldClause = auditFieldBuilder.toString();
        auditFieldClause = auditFieldClause.substring(0, auditFieldClause.length() - 2);
        return auditFieldClause;
    }

    @Override
    @Transactional("masterTransactionManager")
    public void deleteFromMasterData(RelationTable rt, String criteria) {
        TimeTracker tt= new TimeTracker();
        tt.startTracking();
        String query = "DELETE from "+rt.getTableName()+" "+criteria;
        
        List<Object[]> batchArgs = new ArrayList<>();
        Object[] objArr = new Object[1];
        objArr[0]=1;
        batchArgs.add(objArr);
        /*String query = "update "+rt.getTableName()+" set is_archived=? "+criteria; */
        SystemLog.logMessage("Executing query : " + query);
        int [] result = simpleJdbcTemplate.batchUpdate(query, batchArgs);
        SystemLog.logMessage("Result lenght is "+ result.length);
        tt.trackTimeInSeconds("Time taken to delete records from  table : "+ rt.getTableName() +" is : ");
    }
    
    /*@Override
    @Transactional("masterTransactionManager") // change this to masterTransactionalManager once the it is fully tested and ready for production
    public  Set<Object> getPrimaryKeyResultsToBeArchived(RelationTable rt,String criteria,Long limitSize) {
      TimeTracker tt= new TimeTracker();
      tt.startTracking();
      String query = "select "+rt.getPrimaryColumn()+" from "+rt.getTableName()+" "+criteria+" and (is_archived is null or is_archived =0) limit "+limitSize;
      SystemLog.logMessage("Query to be executed is : "+ query);
      Map<String,Object> args = new HashMap<>();
      List<Map<String, Object>> result =  simpleJdbcTemplate.queryForList(query, args);
      tt.trackTimeInSeconds("Time taken to update table : "+ rt.getTableName() +" is : ");
      Set<Object> primaryKeySet = ArchivalUtil.getPropertySetForListOfMap(result, rt.getPrimaryColumn());
      return primaryKeySet;
        
    }
    
    @Override
    @Transactional("masterTransactionManager") // change this to masterTransactionalManager once the it is fully tested and ready for production
    public  Set<Object> getRelatedPrimaryKeyResultToArchive(RelationTable rt) {
        
        TimeTracker tt= new TimeTracker();
        tt.startTracking();
        
        String query = "select t1."+rt.getPrimaryColumn()+" from "+rt.getTableName()+" t1 inner join "+rt.getRelatedToTableName()+" t2  where t1."+rt.getRelationColumn()+"=t2."+rt.getRelatedToColumnName()+" and "
                + "t2.is_archived=1 and (t1.is_archived =0 or t1.is_archived is null) ";
        SystemLog.logMessage("Query to be executed is : "+ query);
        Map<String,Object> args = new HashMap<>();
        List<Map<String, Object>> result =  simpleJdbcTemplate.queryForList(query, args);
        tt.trackTimeInSeconds("Time taken to update table : "+ rt.getTableName() +" is : ");
        Set<Object> primaryKeySet = ArchivalUtil.getPropertySetForListOfMap(result, rt.getPrimaryColumn());
        return primaryKeySet;
        
    }*/
}
