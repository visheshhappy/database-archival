/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.snapdeal.archive.dao.RelationDao;
import com.snapdeal.archive.entity.ExecutionQuery;
import com.snapdeal.archive.entity.ExecutionQuery.QueryType;
import com.snapdeal.archive.util.SystemLog;

import junit.framework.Assert;

/**
 * @version 1.0, 31-Mar-2016
 * @author vishesh
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = { "classpath:/spring/applicationContext.xml" })
public class ArchiveDaoTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // setup the jndi context and the datasource
        try {
            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.naming.java.javaURLContextFactory");
                System.setProperty(Context.URL_PKG_PREFIXES, 
                    "org.apache.naming");         
            
            final SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
            
          
            InitialContext ic = new InitialContext();

            ic.createSubcontext("java:");
            ic.createSubcontext("java:comp");
            ic.createSubcontext("java:comp/env");
            ic.createSubcontext("java:comp/env/jdbc");
           
            MysqlDataSource ds = new  MysqlDataSource();
            ds.setURL("jdbc:mysql://localhost:3306/database_archival?autoReconnect=true");
            ds.setUser("root");
            ds.setPassword("root");
            
            MysqlDataSource ds2 = new  MysqlDataSource();
            ds2.setURL("jdbc:mysql://10.125.1.150:3306/oms_archival_new?autoReconnect=true&amp;rewriteBatchedStatements=true");
            ds2.setUser("root");
            ds2.setPassword("snapdeal");
            
            MysqlDataSource ds3 = new  MysqlDataSource();
            ds3.setURL("jdbc:mysql://30.0.9.193:3306/oms?autoReconnect=true");
            ds3.setUser("vk16903IU");
            ds3.setPassword("Vishesh@123");
            
           /* ic.bind("java:comp/env/jdbc/DatabseArchival", ds);
            ic.bind("java:comp/env/jdbc/OMSArchival", ds2);
            ic.bind("java:comp/env/jdbc/OMSDS", ds3);*/
            
         /*   builder.bind("java:comp/env/jdbc/" + "DatabseArchival", ds);
            builder.bind("java:comp/env/jdbc/" + "OMSArchival", ds2);
            builder.bind("java:comp/env/jdbc/" + "OMSDS", ds3);
            builder.activate();*/
            
        } catch (NamingException ex) {
            //Logger.getLogger(MyDAOTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Autowired
    private RelationDao relationDao;

    @Test
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
