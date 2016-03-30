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
 * @version 1.0, 16-Mar-2016
 * @author vishesh
 */
public interface ArchivalDao {

    RelationTable getRelationShipTableByTableName(String tableName, Integer i)throws BusinessException;

    List<Map<String, Object>> getResult(RelationTable rt, String criteria)throws BusinessException;

    List<Map<String, Object>> getInQueryResult(RelationTable rt, Set inQuerySet)throws BusinessException;

    Integer insertToArchivalDB(RelationTable rt, List<Map<String, Object>> result)throws BusinessException;

    List getArchivalResult(RelationTable rt)throws BusinessException;

    List<RelationTable> getRelations()throws BusinessException;

    void deleteFromMasterData(RelationTable rt, List<Map<String, Object>> result)throws BusinessException;

    Long getCountFromMaster(String tableName, String criteria)throws BusinessException;

    Long getCountFromArchival(String tableName, String criteria)throws BusinessException;

    Long getMasterInQueryCountResult(RelationTable nextRelation, Set inQuerySet)throws BusinessException;

    Long getArchivalInQueryCountResult(RelationTable nextRelation, Set inQuerySet)throws BusinessException;

}
