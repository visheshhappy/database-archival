/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.snapdeal.archive.dao.RelationDao;

/**
 * @version 1.0, 31-Mar-2016
 * @author vishesh
 */
public class ArchiveDaoTest {

   @Mock
   private RelationDao relationDao;
   
   @Before
   public void setUpMock(){
       MockitoAnnotations.initMocks(this);
   }
   
   @Test
   public void testMockCreation(){
       assertNotNull(relationDao);
       
   }
}
