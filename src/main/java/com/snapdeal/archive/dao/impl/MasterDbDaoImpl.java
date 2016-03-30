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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.snapdeal.archive.dao.MasterDbDao;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;
import com.snapdeal.archive.util.SystemLog;
import com.snapdeal.archive.util.TimeTracker;

/**
 * @version 1.0, 29-Mar-2016
 * @author vishesh
 */
@Service
@Transactional("masterTransactionManager")
public class MasterDbDaoImpl implements MasterDbDao {

    @Autowired
    private SimpleJdbcTemplate simpleJdbcTemplate;
    
    @Autowired
    private SimpleJdbcTemplate archivalJdbcTemplate;

    @Override
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

}
