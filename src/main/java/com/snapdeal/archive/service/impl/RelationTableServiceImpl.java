/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.snapdeal.archive.dao.RelationDao;
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

}
