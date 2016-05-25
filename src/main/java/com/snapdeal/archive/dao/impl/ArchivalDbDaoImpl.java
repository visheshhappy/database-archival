/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.snapdeal.archive.dao.ArchivalDbDao;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;
import com.snapdeal.archive.factory.DataBaseFactory;
import com.snapdeal.archive.util.SystemLog;
import com.snapdeal.archive.util.TimeTracker;

/**
 * @version 1.0, 29-Mar-2016
 * @author vishesh
 */
@Service
@Transactional("archivalTransactionManager")
public class ArchivalDbDaoImpl implements ArchivalDbDao {

   @Autowired
    private SimpleJdbcTemplate archivalJdbcTemplate;

    @Autowired
   private SessionFactory     archivalDBSessionFactory;
    
    @Autowired
    private SimpleJdbcTemplate simpleJdbcTemplate;
    
    @Autowired
    private DataBaseFactory databaseFactory;

    @Override
    @Transactional("archivalTransactionManager")
    public Integer insertToArchivalDB(RelationTable rt, List<Map<String, Object>> result) throws BusinessException {

        // if the result to be added in archival db is empty, then simply return
        if (result.isEmpty()) {
            SystemLog.logMessage("No data to be archived for : " + rt.getTableName());
            return 0;
        }

        StringBuilder columnNameBuilder = new StringBuilder();
        StringBuilder placeHolderBuilder = new StringBuilder();

        List<Object[]> batchArgs = new ArrayList<>();
        Boolean isFirst = Boolean.TRUE;
        for (Map<String, Object> map : result) {
            List<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (isFirst) {
                    columnNameBuilder.append(entry.getKey() + ", ");
                    placeHolderBuilder.append("?, ");
                }
                values.add(entry.getValue());
            }
            batchArgs.add(values.toArray());
            isFirst = Boolean.FALSE;
        }
        String columnNames = columnNameBuilder.toString();
        columnNames = columnNames.substring(0, columnNames.length() - 2);

        String placeHolder = placeHolderBuilder.toString();
        placeHolder = placeHolder.substring(0, placeHolder.length() - 2);

        //INSERT INTO FORUMS (FORUM_ID, FORUM_NAME, FORUM_DESC) VALUES (?,?,?)
        String query = "INSERT IGNORE INTO " + rt.getTableName() + " (" + columnNames + " ) VALUES (" + placeHolder + " )";
        SystemLog.logMessage("Inserting data for table...  : " + rt.getTableName() + ", total records are :   " + result.size());
        try {
            TimeTracker tt = new TimeTracker();
            tt.startTracking();
            int[] batchUpdateResult = getArchivalSimpleJdbcTemplate().batchUpdate(query, batchArgs);
            tt.trackTimeInSeconds("^^^^^^^^^^Total time taken to execute update query is  :");
            SystemLog.logMessage("Result is : " + batchUpdateResult.length);
            return batchUpdateResult.length;
        } catch (Exception e) {
            e.printStackTrace();
            SystemLog.logMessage(e.getMessage());
            throw new BusinessException(e);
        }

    }

    @Override
    @Transactional("archivalTransactionManager")
    public List getArchivalResult(RelationTable rt) throws BusinessException {
        try {
            Query query =  getArchivalDBSessionFactory().getCurrentSession().createSQLQuery("select * from " + rt.getTableName()); // TODO : add where clause for date range
            return query.list();
        } catch (Exception e) {
            throw new BusinessException(e);
        }

    }

    @Override
    @Transactional("archivalTransactionManager")
    public Long getCountFromArchival(String tableName, String criteria) throws BusinessException {
        String query = "select count(*) from " + tableName + " " + criteria;
        SystemLog.logMessage("Getting Result for query.!!! \n " + query);
        TimeTracker tt = new TimeTracker();
        tt.startTracking();
        Object[] params = null;
        try {
            List<Map<String, Object>> count = getArchivalSimpleJdbcTemplate().queryForList(query, params);
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
    @Transactional("archivalTransactionManager")
    public Long getArchivalInQueryCountResult(RelationTable rt, Set inQuerySet) throws BusinessException {
        try {
            Map<String, Object> queryParams = new HashMap<String, Object>();
            String query = "select count(*) from " + rt.getTableName() + " where " + rt.getRelationColumn() + " IN (:inQuerySet)";
            queryParams.put("inQuerySet", inQuerySet);
            TimeTracker tt = new TimeTracker();
            tt.startTracking();

            List<Map<String, Object>> result = getArchivalSimpleJdbcTemplate().queryForList(query, queryParams);
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
    @Transactional("archivalTransactionManager")
    public Long getArchivedDataCount(String tableName, String column, Set primaryKeySet) throws BusinessException {
        try {
            if(primaryKeySet.isEmpty()){
                return 0L;
            }
            Map<String, Object> queryParams = new HashMap<String, Object>();
            String query = "select count(*) from " + tableName+ " where " + column + " IN (:inQuerySet)";
            queryParams.put("inQuerySet", primaryKeySet);
            TimeTracker tt = new TimeTracker();
            tt.startTracking();

            List<Map<String, Object>> result = getArchivalSimpleJdbcTemplate().queryForList(query, queryParams);
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
    @Transactional("masterTransactionManager")
    public void alterTable(String tableName, String columnName, String columnType, Boolean toAdd) throws BusinessException {
        try{
            TimeTracker tt = new TimeTracker();
            tt.startTracking();
            String query = "ALTER TABLE "+tableName+" ADD COLUMN "+columnName+" "+columnType;
            Map<String,?> map = new HashMap<>();
            getSimpleJdbcTemplate().update(query, map);
            tt.trackTimeInSeconds("-----------Time taken to alter table"+tableName+" is : ");
        }catch(Exception e){
            SystemLog.logException(e.getMessage());
        }
      
        
    }
    

    private SimpleJdbcTemplate getSimpleJdbcTemplate(){
      //  return databaseFactory.getSimplJdbcTemplate("OmsDs");
        return simpleJdbcTemplate;
    }
    
    private SimpleJdbcTemplate getArchivalSimpleJdbcTemplate(){
     //   return databaseFactory.getSimplJdbcTemplate("OmsArchival");
        return archivalJdbcTemplate;
    }
    
    private SessionFactory getArchivalDBSessionFactory(){
        //return databaseFactory.getSessionFactory("OmsArchival");
        return archivalDBSessionFactory;
    }

}
