/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.snapdeal.archive.dao.RelationDao;
import com.snapdeal.archive.entity.ArchiveInformation;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;
import com.snapdeal.archive.service.RelationTableService;

/**
 * @version 1.0, 21-Apr-2016
 * @author vishesh
 */
@Service
public class RelationTableServiceImpl implements RelationTableService {

    @Autowired
    private RelationDao relationDao;

    @Override
    public RelationTable getRelationTableByTableName(String tableName) throws BusinessException {
        RelationTable rt = relationDao.getRelationShipTableByTableName(tableName, 0);
        return rt;
    }

    @Override
    public RelationTable getRelationTableByArchiveInfoNameAndTableName(String archiveInfoName, String tableName) throws BusinessException {
        RelationTable rt = relationDao.getRelationTableByArchiveInfoNameAndTableName(archiveInfoName,tableName);
        return rt;
    }

    @Override
    public List<ArchiveInformation> getAllArchiveInformations() throws BusinessException{
        List<ArchiveInformation> archiveInformations = relationDao.getAllArchiveInformations();
        return archiveInformations;
    }

}
