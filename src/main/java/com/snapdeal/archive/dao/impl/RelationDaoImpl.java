/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.dao.impl;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.snapdeal.archive.dao.RelationDao;
import com.snapdeal.archive.entity.ArchiveInformation;
import com.snapdeal.archive.entity.DatabaseEntry;
import com.snapdeal.archive.entity.ExecutionQuery;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;
import com.snapdeal.archive.util.SystemLog;

/**
 * @version 1.0, 29-Mar-2016
 * @author vishesh
 */
@Service
@Transactional("transactionManager")
public class RelationDaoImpl implements RelationDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    @Transactional("transactionManager")
    public void saveExecutionQuery(ExecutionQuery query) throws BusinessException {
        try {
            Session session = sessionFactory.getCurrentSession();
           
            query.setCreatedDate(new Date());
            
            // TODO : Dont know why this is not working.. it is generating a query like
            // insert into execution_query (batch_size, complete_query, criteria, batch_start, status, table_name, id) 
            //  values (?, ?, ?, ?, ?, ?, ?)
            // Since id is auto generated, the id column should not be present in this query statement.
            // TODO : resolve this later..THIS IS REALLY BAD.. :(
           //  session.save(query);
           
            
            Query sqlQuery = session.createSQLQuery("insert into execution_query (`table_name`,`batch_start`,`batch_size`,"
                    + "`complete_query`,`status`,`criteria`,`exception_message`,`created_date`,`query_type`) VALUES (\""
                    + query.getTableName() + "\",\"" + query.getStart() + "\",\""
                    + query.getBatchSize() + "\",\"" + query.getCompleteQuery() + "\",\"" + query.getStatus() + "\",\" "+query.getCriteria()+"\",\""+query.getExceptionMessage()+"\",\""+query.getCreatedDate()+"\",\""+query.getQueryType()+"\");");
           sqlQuery.executeUpdate();
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public RelationTable getRelationShipTableByTableName(String tableName, Integer i) throws BusinessException {
        try {
            Query query = sessionFactory.getCurrentSession().createQuery("from RelationTable where level=:lvl and tableName=:tblName");
            query.setParameter("lvl", i);
            query.setParameter("tblName", tableName);
            return (RelationTable) query.uniqueResult();
        } catch (Exception e) {
            throw new BusinessException(e);
        }

    }

    @Override
    public List<RelationTable> getRelations() throws BusinessException {
        try {
            Query query = sessionFactory.getCurrentSession().createQuery("from RelationTable");
            List<RelationTable> results = query.list();
            return results;
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public List<DatabaseEntry> getDatabaseEntries() throws BusinessException {
        Query query = sessionFactory.getCurrentSession().createQuery("from DatabaseEntry where active=:isActive");
        query.setParameter("isActive",true);
        List<DatabaseEntry> results = query.list();
        return results;
    }

    @Override
    public List<RelationTable> getRelationsByArchiveInfoName(String archiveInfoName) throws BusinessException {
        try {
            Query query = sessionFactory.getCurrentSession().createQuery("from ArchiveInformation where name=:archiveName");
            query.setParameter("archiveName",archiveInfoName);
            List<RelationTable> results = query.list();
            return results;
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public RelationTable getRelationTableByArchiveInfoNameAndTableName(String archiveInfoName, String tableName) throws BusinessException {
        try {
           Criteria criteria = sessionFactory.getCurrentSession().createCriteria(RelationTable.class);
           criteria.add(Restrictions.eq("tableName", tableName));
           criteria.createAlias("archiveInformation", "archiveInfo");
           criteria.add(Restrictions.eq("archiveInfo.name", archiveInfoName));
           return (RelationTable) criteria.uniqueResult();
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public List<ArchiveInformation> getAllArchiveInformations() throws BusinessException {
        try {
            Query query = sessionFactory.getCurrentSession().createQuery("from ArchiveInformation");
            List<ArchiveInformation> results = query.list();
            return results;
        } catch (Exception e) {
            SystemLog.logException(e.getMessage());
            throw new BusinessException(e);
        }
    }

}
