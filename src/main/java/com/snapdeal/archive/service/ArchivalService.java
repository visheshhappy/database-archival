/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.service;

import com.snapdeal.archive.entity.RelationTable;

/**
 * @version 1.0, 17-Mar-2016
 * @author vishesh
 */
public interface ArchivalService {

    void deleteMasterData(String tableName, String criteria,Long batchSize);

    boolean verifyArchivedData(String tableName, String criteria,Long batchSize);

    void archieveVerifyAndDeleteData(String tableName, String criteria,Long batchSize);

    Long getCountFromMaster(String tableName, String criteria);

    RelationTable getRelationTableByTableName(String tableName);

    void archieveMasterData(String tableName, String criteria, Long batchSize);

    Long getArchivalCount(String tableName, String baseCriteria);

}
