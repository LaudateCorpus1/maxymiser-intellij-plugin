/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.service;

import com.oracle.maxymiser.intellij.plugin.exception.MaxymiserException;

public interface PushService {
    interface PushProgress {
        void setText(String text);

        void setText2(String text);

        boolean isCancelled();
    }

    class PushResult {
    }

    PushResult push(String baseDir, String path, PushProgress progress) throws MaxymiserException;

    String resolveBaseDir(String path, String basePath);
}
