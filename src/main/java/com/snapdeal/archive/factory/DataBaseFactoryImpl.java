/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import com.snapdeal.archive.DatabaseType;
import com.snapdeal.archive.dao.RelationDao;
import com.snapdeal.archive.entity.ArchiveInformation;
import com.snapdeal.archive.entity.DatabaseEntry;
import com.snapdeal.archive.entity.DatabaseEntry.DatabaseServer;
import com.snapdeal.archive.exception.BusinessException;
import com.snapdeal.archive.util.SystemLog;

/**
 * @version 1.0, 23-May-2016
 * @author vishesh
 */
@Service
public class DataBaseFactoryImpl implements DataBaseFactory {

    @Autowired
    private RelationDao                             relationDao;

    private Map<String, SessionFactory>             sessionFactoryMap;
    private Map<String, SimpleJdbcTemplate>         jdbcTemplateMap;
    private Map<String, PlatformTransactionManager> transactionManagerMap;
    private final static String                     DELIMITER = ":";

    private void addSessionFactory(String name, SessionFactory sf) {
        if (sessionFactoryMap == null) {
            sessionFactoryMap = new HashMap<>();
        }
        sessionFactoryMap.put(name, sf);
    }

    private void addSimpleJdbcTemplate(String name, SimpleJdbcTemplate simpleJdbcTemplate) {
        if (jdbcTemplateMap == null) {
            jdbcTemplateMap = new HashMap<>();
        }
        jdbcTemplateMap.put(name, simpleJdbcTemplate);
    }

    @Override
    public void loadAllDatabaseEntries(List<ArchiveInformation> archiveInformations) {
        SystemLog.logMessage("archiveInformations are : " + archiveInformations.toString());
        initializeState();
        for (ArchiveInformation info : archiveInformations) {
            Set<DatabaseEntry> entries = info.getDatabaseEntries();
            for (DatabaseEntry entry : entries) {
                
                 SessionFactory sf = null;
                Configuration configuration = new Configuration();

                configuration.setProperty("connection.driver_class", entry.getDriverClass());
                if (DatabaseServer.MYSQL.equals(entry.getDatabaseServer())) {
                    configuration.setProperty("hibernate.connection.url", getUrl(entry));
                    configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
                }
                configuration.setProperty("hibernate.current_session_context_class", "org.hibernate.context.ThreadLocalSessionContext");
                
                configuration.setProperty("hibernate.connection.username", entry.getUsername());
                configuration.setProperty("hibernate.connection.password", entry.getPassword());
              
                sf = configuration.buildSessionFactory();
                SystemLog.logMessage("@@@@@@@@@@@@@@@@@@@@@@@@2session factory is  " + sf);
                this.addSessionFactory(createKey(info.getName(), entry.getDatabaseType()), sf);

                PlatformTransactionManager manager = new HibernateTransactionManager(sf);
                this.transactionManagerMap.put(createKey(info.getName(), entry.getDatabaseType()), manager);

                DriverManagerDataSource datasource = new DriverManagerDataSource();
                Properties properties = new Properties();
                properties.setProperty("driverClassName", entry.getDriverClass());
                properties.setProperty("url", getUrl(entry));
                properties.setProperty("username", entry.getUsername());
                properties.setProperty("password", entry.getPassword());
                datasource.setConnectionProperties(properties);
                SimpleJdbcTemplate simpleJdbcTemplate = new SimpleJdbcTemplate(datasource);
                this.addSimpleJdbcTemplate(createKey(info.getName(), entry.getDatabaseType()), simpleJdbcTemplate);

            }
        }

        SystemLog.logMessage("Session factory map is  : ---------------------- " + this.sessionFactoryMap.toString());
        SystemLog.logMessage("Jdbc tempalte map is : --------------------------" + this.jdbcTemplateMap.toString());
        SystemLog.logMessage("transaction manager  map is : --------------------------" + this.transactionManagerMap.toString());
    }

    private void initializeState() {
        this.sessionFactoryMap = new HashMap<>();
        this.jdbcTemplateMap = new HashMap<>();
        this.transactionManagerMap = new HashMap<>();

    }

    @Override
    public SessionFactory getSessionFactory(String name, DatabaseType databaseType) {
        return sessionFactoryMap.get(createKey(name, databaseType));
    }

    @Override
    public SimpleJdbcTemplate getSimplJdbcTemplate(String name, DatabaseType databaseType) {
        return jdbcTemplateMap.get(createKey(name, databaseType));
    }

    private String getUrl(DatabaseEntry entry) {
        switch (entry.getDatabaseServer()) {
            case MYSQL:
                return "jdbc:mysql://" + entry.getIp() + ":" + entry.getPort() + "/" + entry.getDatabaseName() + "?" + entry.getExtraParameter();

            case ORACLE:
                return "";
        }
        return null;

    }

    @PostConstruct
    public void initMethod() throws BusinessException {
        List<ArchiveInformation> archiveInformations = relationDao.getAllArchiveInformations();
        this.loadAllDatabaseEntries(archiveInformations);
    }

    @Override
    public PlatformTransactionManager getTrasactionManager(String contextName, DatabaseType databaseType) {
        return this.transactionManagerMap.get(createKey(contextName, databaseType));
    }

    private String createKey(String contextName, DatabaseType databaseType) {
        String key = contextName + DELIMITER + databaseType.name();
        return key;
    }

}
