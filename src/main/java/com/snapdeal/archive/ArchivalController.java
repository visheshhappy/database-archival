/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.snapdeal.archive.dao.RelationDao;
import com.snapdeal.archive.dto.ArchivalStrategyType;
import com.snapdeal.archive.entity.ArchiveInformation;
import com.snapdeal.archive.entity.RelationTable;
import com.snapdeal.archive.exception.BusinessException;
import com.snapdeal.archive.factory.ArchivalFactory;
import com.snapdeal.archive.service.RelationTableService;
import com.snapdeal.archive.util.ArchivalUtil;
import com.snapdeal.archive.util.SystemLog;

/**
 * @version 1.0, 16-Mar-2016
 * @author vishesh
 */
@Controller
public class ArchivalController {

    @Autowired
    private RelationDao     relationDao;

    /*@Autowired
    private ArchivalService archivalService;*/
    
    @Autowired
    private RelationTableService relationTableService;
    
    @Autowired
    private ArchivalFactory archivalFactory;

    @RequestMapping("/getRelations")
    @ResponseBody
    public List<RelationTable> getRelations() throws BusinessException {

        List<RelationTable> relationTables = relationDao.getRelations();
        return relationTables;
    }
    
    @RequestMapping("/getRelations/{archiveInfoName}")
    @ResponseBody
    public List<RelationTable> getRelationsByArchiveInfo(@PathVariable("archiveInfoName") String archiveInfoName) throws BusinessException {

        List<RelationTable> relationTables = relationDao.getRelationsByArchiveInfoName(archiveInfoName);
        return relationTables;
    }

    @RequestMapping("/home")
    public String homePage(Model model) throws BusinessException {
        List<ArchiveInformation> archiveInformations = relationTableService.getAllArchiveInformations();
        Set<String> names = ArchivalUtil.getPropertySet(archiveInformations, "name", String.class);
        model.addAttribute("names", names);
        return "home";
    }

    @RequestMapping("/getRelation/{archiveInfoName}/{tableName}")
    @ResponseBody
    public RelationTable getRelationByTableName(@PathVariable("archiveInfoName") String archiveInfoName,@PathVariable("tableName") String tableName) throws BusinessException {
        RelationTable rt = relationTableService.getRelationTableByArchiveInfoNameAndTableName(archiveInfoName,tableName);
        return rt;
    }

    @RequestMapping("/execute")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String batchSize = request.getParameter("batchSize");
        String tableName = request.getParameter("tableName");
        switch (action) {
            case "archive":
                request.getRequestDispatcher("/archivedata/" + tableName + "/" + batchSize).forward(request, response);
                break;
            case "delete":
                request.getRequestDispatcher("/delete/" + tableName + "/" + batchSize).forward(request, response);
                break;
            case "archieveAndDelete":
                request.getRequestDispatcher("/archive/verify/delete/" + tableName + "/" + batchSize).forward(request, response);
                break;
            case "count":
                request.getRequestDispatcher("/count/" + tableName).forward(request, response);
                break;
            default:
                break;
        }

        return "home";
    }

    @RequestMapping("/archivedata/{tableName}/{batchSize}")
    public String archieveData(HttpServletRequest request, @PathVariable("tableName") String tableName, @PathVariable("batchSize") String batchSize)
            throws NumberFormatException, BusinessException {
        SystemLog.logMessage("Table name is  : " + tableName);
        String criteria = "where " + request.getParameter("whereClause");
        String strategy = request.getParameter("strategy");
        String archiveInfoName = request.getParameter("archiveInfoName");
        ArchivalStrategyType archivalStrategyType = getStrategyType(strategy);
        archivalFactory.getArchivalService(archivalStrategyType).archieveMasterData(tableName, criteria, Long.valueOf(batchSize),archiveInfoName);
     //   archivalService.archieveMasterData(tableName, criteria, Long.valueOf(batchSize));
        return "home";
    }

    private ArchivalStrategyType getStrategyType(String strategy) {
        if(strategy.equalsIgnoreCase("columnStrategy")){
            return ArchivalStrategyType.COLUMN_STRATEGY;
        }
        if(strategy.equalsIgnoreCase("Direct_DbPagination")){
            return ArchivalStrategyType.DIRECT_WITH_DB_PAGINATION_STRATEGY;
        }
        if(strategy.equalsIgnoreCase("Direct_SystemCache")){
            return ArchivalStrategyType.DIRECT_WITH_JAVA_PAGINATION_STRATEGY;
        }
        return null;
    }

    @RequestMapping("/archive/verify/delete/{tableName}/{batchSize}")
    public String archieveVerifyAndDeleteData(HttpServletRequest request, @PathVariable("tableName") String tableName, @PathVariable("batchSize") String batchSize)
            throws BusinessException {
        SystemLog.logMessage("Table name is  : " + tableName);
        String criteria = "where " + request.getParameter("whereClause");
        Long batch = Long.valueOf(batchSize);
        String strategy = request.getParameter("strategy");
        String archiveInfoName = request.getParameter("archiveInfoName");
        ArchivalStrategyType archivalStrategyType = getStrategyType(strategy);
        archivalFactory.getArchivalService(archivalStrategyType).archieveVerifyAndDeleteData(tableName, criteria, batch,archiveInfoName);
        return "home";
    }

    @RequestMapping("/delete/{tableName}/{batchSize}")
    public String deleteData(HttpServletRequest request, @PathVariable("tableName") String tableName, @PathVariable("batchSize") String batchSize) throws BusinessException {
        SystemLog.logMessage("Table name is  : " + tableName);
        String criteria = "where " + request.getParameter("whereClause");
        Long batch = Long.valueOf(batchSize);
        String strategy = request.getParameter("strategy");
        String archiveInfoName = request.getParameter("archiveInfoName");
        ArchivalStrategyType archivalStrategyType = getStrategyType(strategy);
        archivalFactory.getArchivalService(archivalStrategyType).deleteMasterData(tableName, criteria, batch,archiveInfoName);
        return "home";
    }

    @RequestMapping("/count/{tableName}")
    @ResponseBody
    public Long getCount(HttpServletRequest request, @PathVariable("tableName") String tableName) throws BusinessException {
        SystemLog.logMessage("Table name is  : " + tableName);
        String criteria = "where " + request.getParameter("whereClause");
        String strategy = request.getParameter("strategy");
        String archiveInfoName = request.getParameter("archiveInfoName");
        ArchivalStrategyType archivalStrategyType = getStrategyType(strategy);
        Long count = archivalFactory.getArchivalService(archivalStrategyType).getCountFromMaster(tableName, criteria);
        return count;
    }

}
