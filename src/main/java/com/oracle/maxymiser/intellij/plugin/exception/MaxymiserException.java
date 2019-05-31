/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.exception;

public class MaxymiserException extends Exception {
    public MaxymiserException() {
    }

    public MaxymiserException(String message) {
        super(message);
    }

    public MaxymiserException(String message, Throwable cause) {
        super(message, cause);
    }

    public MaxymiserException(Throwable cause) {
        super(cause);
    }

    public MaxymiserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
