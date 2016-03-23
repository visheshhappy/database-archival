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

    void deleteData(String tableName, String criteria);

    boolean verifyData(String tableName, String criteria);

    void archieveVerifyAndDeleteData(String tableName, String criteria);

    Long getCount(String tableName, String criteria);

    RelationTable getRelationTableByTableName(String tableName);

    void archieveMasterData(String tableName, String criteria, Long batchSize);

}
