/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.util;

/**
 * @version 1.0, 21-Mar-2016
 * @author vishesh
 */
public class TimeTracker {

    long time1;
    long time2;

    public void startTracking() {
        time1 = System.currentTimeMillis();
    }

    public void trackTimeInSeconds(String message) {
        time2 = System.currentTimeMillis();
        double totalTime = (time2 - time1) / 1000d;
        SystemLog.logMessage(message + " " + totalTime + " seconds");
    }
    
    public void trackTimeInMinutes(String message) {
        time2 = System.currentTimeMillis();
        double totalTime = ((time2 - time1) / 1000d)/60d;
        SystemLog.logMessage(message + " " + totalTime + " minutes");
    }
}
