/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.service.impl;

import com.intellij.openapi.project.Project;
import com.oracle.maxymiser.intellij.plugin.service.RestService;
import com.oracle.maxymiser.intellij.plugin.service.TaskManagerService;
import com.oracle.maxymiser.intellij.plugin.service.UIService;
import com.oracle.maxymiser.intellij.plugin.ui.CampaignChooser;

public class UIServiceImpl implements UIService {
    private final RestService restService;
    private final TaskManagerService taskManagerService;

    public UIServiceImpl(RestService restService, TaskManagerService taskManagerService) {
        this.restService = restService;
        this.taskManagerService = taskManagerService;
    }

    @Override
    public GetCampaignResult getCampaign(Project project) {
        CampaignChooser campaignChooser = new CampaignChooser(this.restService, this.taskManagerService, project);

        boolean isOk = campaignChooser.showAndGet();

        return isOk ? new GetCampaignResult(campaignChooser.getSite(), campaignChooser.getCampaign()) : null;
    }
}
