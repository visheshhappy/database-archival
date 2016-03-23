/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.snapdeal.archive.entity.RelationTable;

/**
 * @version 1.0, 16-Mar-2016
 * @author vishesh
 */
public interface ArchivalDao {

     RelationTable getRelationShipTableByTableName(String tableName, Integer i);

     List<Map<String, Object>> getResult(RelationTable rt,String criteria);

     List<Map<String, Object>>  getInQueryResult(RelationTable rt, Set inQuerySet);

     void insertToArchivalDB(RelationTable rt, List<Map<String, Object>> result);

     List getArchivalResult(RelationTable rt);
    
     List<RelationTable> getRelations();

     void deleteFromMasterData(RelationTable rt, List<Map<String, Object>> result);
     
     Long getCountFromMaster(String tableName, String criteria);

    Long getCountFromArchival(String tableName, String criteria);

}
