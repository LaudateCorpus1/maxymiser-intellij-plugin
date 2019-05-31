/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.ui.support;

import com.intellij.openapi.diagnostic.Logger;
import com.oracle.maxymiser.intellij.plugin.exception.MaxymiserException;
import com.oracle.maxymiser.intellij.plugin.model.Campaign;
import com.oracle.maxymiser.intellij.plugin.model.Site;
import com.oracle.maxymiser.intellij.plugin.service.RestService;
import com.oracle.maxymiser.intellij.plugin.service.TaskManagerService;

import javax.swing.*;

public class CampaignsListModel {
    private final RestService restService;
    private final TaskManagerService taskManagerService;
    private final DefaultListModel<Campaign> listModel;
    private final CampaignsListModelListener listener;
    private final Logger LOG = Logger.getInstance(CampaignsListModel.class);
    private Site site;

    public CampaignsListModel(RestService restService, TaskManagerService taskManagerService, CampaignsListModelListener listener) {
        this.restService = restService;
        this.taskManagerService = taskManagerService;
        this.listModel = new DefaultListModel<>();
        this.listener = listener;
    }

    public ListModel<Campaign> getListModel() {
        return this.listModel;
    }

    public void setSite(Site site) {
        this.listModel.clear();
        this.site = site;
        this.fetch();
    }

    public void init() {
        fetch();
    }

    private void fetch() {
        if (this.site != null) {
            listener.onFetchingCampaigns();

            this.taskManagerService.async(() -> {
                return restService.readCampaigns(site.getId());
            }).onSuccess(campaigns -> {
                for (Campaign campaign : campaigns) {
                    listModel.addElement(campaign);
                }
                listener.onFetchingCampaignsCompleted();
            }).onError(throwable -> {
                if (throwable instanceof MaxymiserException) {
                    listener.onFetchingCampaignsFailed(throwable.getMessage());
                } else {
                    LOG.error("Failed to fetch campaigns", throwable);
                    listener.onFetchingCampaignsFailed("Unexpected error");
                }
            });
        } else {
            this.listModel.clear();
        }

    }
}
