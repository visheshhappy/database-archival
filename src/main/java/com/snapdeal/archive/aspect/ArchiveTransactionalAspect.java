/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */  
package com.snapdeal.archive.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.snapdeal.archive.DatabaseType;
import com.snapdeal.archive.annotation.ArchiveTransactional;
import com.snapdeal.archive.dto.ArchivalContextDto;
import com.snapdeal.archive.factory.DataBaseFactory;
import com.snapdeal.archive.util.ArchivalContext;

/**
 *  
 *  @version     1.0, 25-May-2016
 *  @author vishesh
 */
@Aspect
@Component
public class ArchiveTransactionalAspect {
    
    @Autowired
    private DataBaseFactory databaseFactory;
    
    @Pointcut("@annotation(com.snapdeal.archive.annotation.ArchiveTransactional)")
    public void archiveTransactional() {
    }
    
    @Around("archiveTransactional()")
    public Object archiveTransactionAspect(final ProceedingJoinPoint pjp) throws Throwable{
        ArchivalContextDto dto = ArchivalContext.getContext();
        ArchiveTransactional annotation = getMethodAnnotation(pjp);
        DatabaseType databaseType = annotation.transactionOn(); // get this from annotation property
        PlatformTransactionManager transactionManager = databaseFactory.getTrasactionManager(dto.getContextName(),databaseType);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        
        
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
     // explicitly setting the transaction name is something that can only be done programmatically
         def.setName("SomeTxName");
         def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    
         TransactionStatus status = transactionManager.getTransaction(def);
         Object obj = null;
         try {
            obj = pjp.proceed();
         }
         catch ( Throwable ex) {
             transactionManager.rollback(status);
             throw ex;
         }
         transactionManager.commit(status);
         return obj;
     
     
     
        
      /*  return transactionTemplate.execute(new TransactionCallback() {
            // the code in this method executes in a transactional context
            public Object doInTransaction(TransactionStatus status) {
                return status;
               
            }
        });*/
    }
    
    private ArchiveTransactional getMethodAnnotation(ProceedingJoinPoint pjp) {
        Signature s = pjp.getSignature();
        Method reqMethod = null;
        Method[] methods = s.getDeclaringType().getDeclaredMethods();
        String reqMethodName = s.getName();
        for (Method m : methods) {
            if (m.getName().equals(reqMethodName)) {
                reqMethod = m;
                break;
            }
        }
        ArchiveTransactional annotation = reqMethod.getAnnotation(ArchiveTransactional.class);
        return annotation;
    }

}
