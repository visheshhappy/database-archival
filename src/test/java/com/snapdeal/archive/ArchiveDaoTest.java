/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.snapdeal.archive.dao.RelationDao;
import com.snapdeal.archive.entity.ExecutionQuery;
import com.snapdeal.archive.entity.ExecutionQuery.QueryType;

import junit.framework.Assert;

/**
 * @version 1.0, 31-Mar-2016
 * @author vishesh
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/WEB-INF/spring/applicationContext.xml" })
public class ArchiveDaoTest {

    @Autowired
    private RelationDao relationDao;

  //  @Test
    public void testRelationDao() {
        ExecutionQuery eq = new ExecutionQuery();
        eq.setBatchSize(100L);
        eq.setQueryType(QueryType.INSERT);
        eq.setTableName("sale_order");
        Exception ex = null;
        try {
            relationDao.saveExecutionQuery(eq);
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertEquals(null, ex);
    }

    @Test
    public void testArchiving() {

    }

}
