/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.snapdeal.archive.util.SystemLog;

/**
 * @version 1.0, 02-May-2016
 * @author vishesh
 */
@Aspect
@Component
public class ArchivingExceptionHandlingAspect {
    
    @Value("${max.retry.count}")
    private Integer maxRetryCount; 
    
    @Pointcut("execution(* com.snapdeal.archive.dao.impl.MasterDbDaoImpl.mark*(..))")
    public void markArchive() {
    }

    @Pointcut("execution(* com.snapdeal.archive.dao.impl.ArchivalDbDaoImpl.insertToArchivalDB(..))")
    public void insertIntoArchivalDb() {
    }

    @Pointcut("execution(* com.snapdeal.archive.service.AbstractArchivalService+.verifyBasedOnCount(..))")
    public void verifyCount() {
    }

    @Pointcut("execution(* com.snapdeal.archive.dao.impl.MasterDbDaoImpl.deleteFromMasterData(..))")
    public void deleteData() {
    }

    /**
     * This method is a retry mechanism for various method, It will retry the method the given number of times in case
     * of exception
     * 
     * @param pjp
     * @throws Throwable
     */
    @Around("markArchive()  || insertIntoArchivalDb() || verifyCount() || deleteData()")
    public void wrapExceptionHandling(ProceedingJoinPoint pjp) throws Throwable {

        int i = 0;
        while (i < maxRetryCount) {
            try {
                pjp.proceed();
                break;
            } catch (Throwable e) {
                i++;
                int tryCount = i+1;
                SystemLog.logMessage("Some exception occurred ivoking method " + pjp.getSignature().getName() + ". Trying for " +tryCount + " time");
                if (i == maxRetryCount-1) {
                    SystemLog.logException("Cannot complete the method " + pjp.getSignature().getName() + " evene after trying "+maxRetryCount+" times...");
                    throw e;
                }

            }
        }

    }
}
