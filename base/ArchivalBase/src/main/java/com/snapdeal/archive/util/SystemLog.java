/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */  
package com.snapdeal.archive.util;

import java.util.Date;

/**
 *  
 *  @version     1.0, 21-Mar-2016
 *  @author vishesh
 */
public class SystemLog {
    
    public static void logMessage(String message){
        Date d = new Date();
        System.out.println(d.toString()+" : "+message);
    }

    public static void logException(String message) {
        System.out.println("++++++++++++++++++++LOGGING EXCEPTION++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        Date d = new Date();
        System.out.println(d.toString()+" : "+message);
        System.out.println("++++++++++++++++++++EXCEPTION LOGGING FINISH+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

}
