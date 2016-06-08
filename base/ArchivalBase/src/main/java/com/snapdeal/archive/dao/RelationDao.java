/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */  
package com.snapdeal.archive.dao;

import java.util.List;

import com.snapdeal.archive.entity.ArchiveInformation;
import com.snapdeal.archive.entity.DatabaseEntry;
import com.snapdeal.archive.entity.ExecutionQuery;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;

/**
 *  
 *  @version     1.0, 29-Mar-2016
 *  @author vishesh
 */
public interface RelationDao {
    
    void saveExecutionQuery(ExecutionQuery query) throws BusinessException;
    RelationTable getRelationShipTableByTableName(String tableName, Integer i)throws BusinessException;
    List<RelationTable> getRelations()throws BusinessException;
    List<DatabaseEntry> getDatabaseEntries() throws BusinessException;
    List<RelationTable> getRelationsByArchiveInfoName(String archiveInfoName) throws BusinessException;
    RelationTable getRelationTableByArchiveInfoNameAndTableName(String archiveInfoName, String tableName) throws BusinessException;
    List<ArchiveInformation> getAllArchiveInformations()throws BusinessException;

}
