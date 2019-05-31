/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.ui.support;

public interface CampaignsListModelListener {
    void onFetchingCampaigns();

    void onFetchingCampaignsCompleted();

    void onFetchingCampaignsFailed(String message);
}
