/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */  
package com.snapdeal.archive.util;

import com.snapdeal.archive.dto.ArchivalContextDto;

/**
 *  
 *  @version     1.0, 25-May-2016
 *  @author vishesh
 */
public class ArchivalContext {
    
    private static ThreadLocal<ArchivalContextDto> context = new InheritableThreadLocal<ArchivalContextDto>() {
        protected ArchivalContextDto initialValue() {
            return new ArchivalContextDto();
        }
    };
    
    public static void setContext(ArchivalContextDto contextDto){
        context.set(contextDto);
    }
    
    public static ArchivalContextDto getContext(){
        return context.get();
    }
    
    public static void clearContext(){
        context.remove();
    }

}
