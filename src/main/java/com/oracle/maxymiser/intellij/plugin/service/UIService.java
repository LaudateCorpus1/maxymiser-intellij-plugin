/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.service;

import com.intellij.openapi.project.Project;
import com.oracle.maxymiser.intellij.plugin.model.Campaign;
import com.oracle.maxymiser.intellij.plugin.model.Site;

public interface UIService {

    class GetCampaignResult {
        private final Campaign campaign;
        private final Site site;

        public GetCampaignResult(Site site, Campaign campaign) {
            this.campaign = campaign;
            this.site = site;
        }

        public Campaign getCampaign() {
            return this.campaign;
        }

        public Site getSite() {
            return this.site;
        }
    }

    GetCampaignResult getCampaign(Project project);
}
