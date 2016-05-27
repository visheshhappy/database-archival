/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.snapdeal.archive.dto.ArchivalContextDto;
import com.snapdeal.archive.util.ArchivalContext;

/**
 * @version 1.0, 25-May-2016
 * @author vishesh
 */
public class ArchiveContextFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {
            // TODO : hardcoding should be removed
            String archiveInfoName = request.getParameter("archiveInfoName");
            ArchivalContextDto dto = new ArchivalContextDto();
            dto.setContextName(archiveInfoName);
            ArchivalContext.setContext(dto);
            chain.doFilter(request, response);
        } finally {
            ArchivalContext.clearContext();
        }

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
