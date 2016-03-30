/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.service;

import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;

/**
 * @version 1.0, 17-Mar-2016
 * @author vishesh
 */
public interface ArchivalService {

    void deleteMasterData(String tableName, String criteria,Long batchSize) throws BusinessException ;

    boolean verifyArchivedData(String tableName, String criteria,Long batchSize) throws BusinessException ;

    void archieveVerifyAndDeleteData(String tableName, String criteria,Long batchSize) throws BusinessException ;

    Long getCountFromMaster(String tableName, String criteria) throws BusinessException ;

    RelationTable getRelationTableByTableName(String tableName) throws BusinessException ;

    void archieveMasterData(String tableName, String criteria, Long batchSize) throws BusinessException ;

    Long getArchivalCount(String tableName, String baseCriteria) throws BusinessException ;

}
