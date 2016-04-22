/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.snapdeal.archive.dto.ArchivalStrategyType;
import com.snapdeal.archive.service.ArchivalService;

/**
 * @version 1.0, 20-Apr-2016
 * @author vishesh
 */
@Service
public class ArchivalFactory {

    @Autowired
    @Qualifier("archivalService")
    private ArchivalService archivalService;

    @Autowired
    @Qualifier("archivalServiceUpdateColumn")
    private ArchivalService archivalServiceUpdateColumn;

    public ArchivalService getArchivalService(ArchivalStrategyType strategyType) {
        switch (strategyType) {
            case COLUMN_STRATEGY:
                return archivalServiceUpdateColumn;
            case DIRECT_WITH_DB_PAGINATION_STRATEGY:
                return archivalService;
            case DIRECT_WITH_JAVA_PAGINATION_STRATEGY:
                return null;
        }
        return null;
    }

}
