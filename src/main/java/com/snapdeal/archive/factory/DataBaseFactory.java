/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.factory;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.snapdeal.archive.entity.DatabaseEntry;

/**
 * @version 1.0, 23-May-2016
 * @author vishesh
 */
public interface DataBaseFactory {

    void loadAllDatabaseEntries(List<DatabaseEntry> entries);

    SessionFactory getSessionFactory(String name);

    SimpleJdbcTemplate getSimplJdbcTemplate(String name);

}
