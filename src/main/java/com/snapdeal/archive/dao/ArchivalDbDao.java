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
public interface ArchivalDbDao {

    Integer insertToArchivalDB(RelationTable rt, List<Map<String, Object>> result) throws BusinessException;

    List getArchivalResult(RelationTable rt) throws BusinessException;

    Long getCountFromArchival(String tableName, String criteria) throws BusinessException;

    Long getArchivalInQueryCountResult(RelationTable nextRelation, Set inQuerySet) throws BusinessException;

    Long getArchivedDataCount(String tableName, String column, Set primaryKeySet) throws BusinessException;

}
