/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.snapdeal.archive.exception;

/**
 * @version 1.0, 29-Mar-2016
 * @author vishesh
 */
public class BusinessException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -3680582591437720326L;

    private String            message;
    private Throwable         error;

    public BusinessException(Throwable t) {
        this.error = t;
    }

    public BusinessException(String message, Throwable t) {
        this.error = t;
        this.message = message;
    }

    public BusinessException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getError() {
        return error;
    }

}
