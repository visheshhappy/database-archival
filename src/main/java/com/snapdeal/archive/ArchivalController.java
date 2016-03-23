/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */  
package com.snapdeal.archive;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.snapdeal.archive.dao.ArchivalDao;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.service.ArchivalService;
import com.snapdeal.archive.util.SystemLog;

/**
 *  
 *  @version     1.0, 16-Mar-2016
 *  @author vishesh
 */
@Controller
public class ArchivalController {
    
    @Autowired
    private ArchivalDao archivalDao;
    
    @Autowired
    private ArchivalService archivalService;
    
    private static int paginationSize = 100;
    
    @RequestMapping("/getRelations")
    @ResponseBody
    public List<RelationTable> getRelations(){
        
        List<RelationTable> relationTables= archivalDao.getRelations();
        return  relationTables;
    }
    
    @RequestMapping("/")
    public String demoPage(){
        
        return "index.html";
    }
    
    @RequestMapping("/archivedata/{tableName}/{batchSize}")
    public void archieveData(@PathVariable("tableName") String tableName,@PathVariable("batchSize") String batchSize){
        SystemLog.logMessage("Table name is  : " + tableName);
        String criteria = "where created<='2012-12-31' ";
        archivalService.archieveMasterData(tableName,criteria,Long.valueOf(batchSize));    
    }
    
    @RequestMapping("/archive/verify/delete/{tableName}")
    public void archieveVerifyAndDeleteData(@PathVariable("tableName") String tableName){
        SystemLog.logMessage("Table name is  : " + tableName);
        String criteria = "where created<='2012-12-31'";
        archivalService.archieveVerifyAndDeleteData(tableName,criteria);
    }
    
    @RequestMapping("/delete/{tableName}")
    public void deleteData(@PathVariable("tableName") String tableName){
        SystemLog.logMessage("Table name is  : " + tableName);
        String criteria = "where created<='2012-12-31'";
        archivalService.deleteData(tableName,criteria);
    }
    
    @RequestMapping("/count/{tableName}")
    @ResponseBody
    public Long getCount(@PathVariable("tableName") String tableName){
        SystemLog.logMessage("Table name is  : " + tableName);
        String criteria = "where created<='2012-12-31'";
        Long count = archivalService.getCount(tableName,criteria);
        return count;
    }
    
    
    

}
