/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.snapdeal.archive.dao.ArchivalDbDao;
import com.snapdeal.archive.dao.MasterDbDao;
import com.snapdeal.archive.dto.ExecutionStats;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;
import com.snapdeal.archive.util.ArchivalUtil;
import com.snapdeal.archive.util.SystemLog;

/**
 * @version 1.0, 21-Apr-2016
 * @author vishesh
 */
public abstract class AbstractArchivalService implements ArchivalService {

    @Autowired
    private MasterDbDao   masterDbDao;

    @Autowired
    private ArchivalDbDao archivalDbDao;

    @Override
    public abstract void deleteMasterData(String tableName, String criteria, Long batchSize) throws BusinessException;

    @Override
    public abstract boolean verifyArchivedData(String tableName, String criteria, Long batchSize) throws BusinessException;

    @Override
    public abstract void archieveVerifyAndDeleteData(String tableName, String criteria, Long batchSize) throws BusinessException;

    @Override
    public Long getCountFromMaster(String tableName, String criteria) throws BusinessException {
        Long count = masterDbDao.getCountFromMaster(tableName, criteria);
        return count;
    }

    @Override
    public Long getArchivalCount(String tableName, String baseCriteria) throws BusinessException {
        Long count = archivalDbDao.getCountFromArchival(tableName, baseCriteria);
        return count;
    }

    @Override
    public abstract void archieveMasterData(String tableName, String baseCriteria, Long batchSize) throws BusinessException;
    
    protected Boolean verifyBasedOnCount(Map<RelationTable, List<Map<String, Object>>> tableResultMap) throws BusinessException {

        SystemLog.logMessage("Calling verifyBasedOnCount() method : " );

        for (RelationTable rt : tableResultMap.keySet()) {
            List<Map<String,Object>> resultMap =  tableResultMap.get(rt);
            
            String primaryKey = rt.getPrimaryColumn();
            
            Set primaryKeySet = ArchivalUtil.getPropertySetForListOfMap(resultMap, primaryKey);
            Long archivedDataCount = archivalDbDao.getArchivedDataCount(rt.getTableName(),primaryKey,primaryKeySet);
            Integer masterDataSize =  tableResultMap.get(rt).size();
            
            SystemLog.logMessage("Master count for table :"+rt.getTableName()+" is : "+masterDataSize+". Archival count = "+archivedDataCount);
            
            if (masterDataSize.longValue() != archivedDataCount) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

}
