/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;

/**
 * @version 1.0, 29-Mar-2016
 * @author vishesh
 */
public interface MasterDbDao {

    List<Map<String, Object>> getResult(RelationTable rt, String criteria) throws BusinessException;

    List<Map<String, Object>> getInQueryResult(RelationTable rt, Set inQuerySet) throws BusinessException;

    void deleteFromMasterData(RelationTable rt, List<Map<String, Object>> result) throws BusinessException;

    Long getCountFromMaster(String tableName, String criteria) throws BusinessException;

    Long getMasterInQueryCountResult(RelationTable nextRelation, Set inQuerySet) throws BusinessException;

    
    
    
    // The method below uses is_archived strategy
    void markResultsToBeArchived(RelationTable rt,String criteria, Long limitSize);
    
    void markRelatedResultToArchive(RelationTable rt, Set<Object> primaryKeyNotInSet);

    Set<Object> getRelatedPrimaryKeyResultToArchive(RelationTable rt);

    Set<Object> getPrimaryKeyResultsToBeArchived(RelationTable rt, String criteria, Long limitSize);

}
