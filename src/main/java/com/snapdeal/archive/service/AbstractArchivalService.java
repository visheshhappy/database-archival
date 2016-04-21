/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.snapdeal.archive.dao.ArchivalDbDao;
import com.snapdeal.archive.dao.MasterDbDao;
import com.snapdeal.archive.exception.BusinessException;

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

}
