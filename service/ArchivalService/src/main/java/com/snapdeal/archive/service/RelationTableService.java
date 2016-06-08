/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */  
package com.snapdeal.archive.service;

import java.util.List;

import com.snapdeal.archive.entity.ArchiveInformation;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;

/**
 *  
 *  @version     1.0, 21-Apr-2016
 *  @author vishesh
 */
public interface RelationTableService {
    
    RelationTable getRelationTableByTableName(String tableName) throws BusinessException ;

    RelationTable getRelationTableByArchiveInfoNameAndTableName(String archiveInfoName, String tableName) throws BusinessException;

    List<ArchiveInformation> getAllArchiveInformations() throws BusinessException;

}
