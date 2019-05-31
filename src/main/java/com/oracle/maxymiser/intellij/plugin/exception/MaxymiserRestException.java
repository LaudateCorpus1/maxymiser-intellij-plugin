/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.exception;

import com.oracle.maxymiser.intellij.plugin.model.Error;
import com.oracle.maxymiser.intellij.plugin.model.ErrorResponse;

public class MaxymiserRestException extends MaxymiserException {
    private final ErrorResponse errorResponse;

    public MaxymiserRestException(ErrorResponse errorResponse) {
        super();
        this.errorResponse = errorResponse;
    }


    public MaxymiserRestException(String message, ErrorResponse errorResponse) {
        super(message);
        this.errorResponse = errorResponse;
    }

    public MaxymiserRestException(String message) {
        super(message);
        this.errorResponse = null;
    }

    public MaxymiserRestException(String message, Throwable cause) {
        super(message, cause);
        this.errorResponse = null;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    @Override
    public String getMessage() {
        if (errorResponse != null && errorResponse.getErrorDescription() != null) {
            return errorResponse.getErrorDescription();
        }
        if (errorResponse != null && errorResponse.getErrors() != null) {
            StringBuilder sb = new StringBuilder();
            String separator = "";
            for (Error error : errorResponse.getErrors()) {

                sb.append("Code: ").append(error.getCode()).append(", Message: ").append(error.getMessage()).append(", Parameter: ").append(error.getParameter());
                sb.append(separator);
                separator = "/n";
            }
            return sb.toString();
        }

        return super.getMessage();
    }
}
