/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */  
package com.snapdeal.archive;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.snapdeal.archive.dao.RelationDao;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;
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
    private RelationDao relationDao;
    
    @Autowired
    private ArchivalService archivalService;
        
    @RequestMapping("/getRelations")
    @ResponseBody
    public List<RelationTable> getRelations() throws BusinessException{
        
        List<RelationTable> relationTables= relationDao.getRelations();
        return  relationTables;
    }
    
    @RequestMapping("/home")
    public String homePage(){
        
        return "home";
    }

    @RequestMapping("/execute")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String batchSize = request.getParameter("batchSize");
        String tableName = request.getParameter("tableName");
        switch (action) {
            case "archive":
                request.getRequestDispatcher("/archivedata/"+tableName+"/"+batchSize).forward(request, response);
                break;
            case "delete":
                request.getRequestDispatcher("/delete/"+tableName+"/"+batchSize).forward(request, response);
                break;
            case "archieveAndDelete":
                request.getRequestDispatcher("/archive/verify/delete/"+tableName+"/"+batchSize).forward(request, response);
                break;
            case "count":
                request.getRequestDispatcher("/count/"+tableName).forward(request, response);
                break;
            default:
                break;
        }

        return "home";
    }
    
    @RequestMapping("/archivedata/{tableName}/{batchSize}")
    public String archieveData(HttpServletRequest request,@PathVariable("tableName") String tableName,@PathVariable("batchSize") String batchSize) throws NumberFormatException, BusinessException{
        SystemLog.logMessage("Table name is  : " + tableName);
       // String criteria = "where created<='2011-12-31' ";
        
        String criteria = "where "+ request.getParameter("whereClause") ;
        archivalService.archieveMasterData(tableName,criteria,Long.valueOf(batchSize));    
        return "home";
    }
    
    @RequestMapping("/archive/verify/delete/{tableName}/{batchSize}")
    public String archieveVerifyAndDeleteData(HttpServletRequest request,@PathVariable("tableName") String tableName,@PathVariable("batchSize") String batchSize) throws BusinessException{
        SystemLog.logMessage("Table name is  : " + tableName);
        //String criteria = "where created<='2012-12-31'";
        String criteria = "where "+ request.getParameter("whereClause") ;
        Long batch = Long.valueOf(batchSize);
        archivalService.archieveVerifyAndDeleteData(tableName,criteria,batch);
        return "home";
    }
    
    @RequestMapping("/delete/{tableName}/{batchSize}")
    public String deleteData(HttpServletRequest request,@PathVariable("tableName") String tableName,@PathVariable("batchSize") String batchSize) throws BusinessException{
        SystemLog.logMessage("Table name is  : " + tableName);
    //    String criteria = "where created<='2012-12-31'";
        String criteria = "where "+ request.getParameter("whereClause") ;
        Long batch = Long.valueOf(batchSize);
        archivalService.deleteMasterData(tableName,criteria,batch);
        return "home";
    }
    
    @RequestMapping("/count/{tableName}")
    @ResponseBody
    public Long getCount(HttpServletRequest request, @PathVariable("tableName") String tableName) throws BusinessException{
        SystemLog.logMessage("Table name is  : " + tableName);
        String criteria = "where "+ request.getParameter("whereClause") ;//"where created<='2012-12-31'";
        Long count = archivalService.getCountFromMaster(tableName,criteria);
        return count;
    }
    
    
    

}
