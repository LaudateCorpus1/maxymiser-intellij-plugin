/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.service;

import com.oracle.maxymiser.intellij.plugin.exception.MaxymiserException;

public interface PullService {
    class PullResult {

    }

    interface PullProgress {
        void setText(String text);

        boolean isCancelled();
    }

    String init(String campaignName, String siteId, String campaignId, String path);

    PullResult pull(String baseDir, String path, PullProgress progress) throws MaxymiserException;

    String resolveBaseDir(String path, String root);
}
