/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.model;

import java.util.Arrays;
import java.util.List;

public class ErrorResponse {
    private String error;
    private String error_description;
    private Error[] errors;

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return error_description;
    }

    public List<Error> getErrors() {
        return Arrays.asList(errors);
    }
}
