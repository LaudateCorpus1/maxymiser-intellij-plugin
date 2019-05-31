/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.ui.support;

import com.intellij.openapi.diagnostic.Logger;
import com.oracle.maxymiser.intellij.plugin.exception.MaxymiserException;
import com.oracle.maxymiser.intellij.plugin.model.Site;
import com.oracle.maxymiser.intellij.plugin.service.RestService;
import com.oracle.maxymiser.intellij.plugin.service.TaskManagerService;

import javax.swing.*;

public class SitesListModel {
    private final RestService restService;
    private final TaskManagerService taskManagerService;
    private final DefaultListModel<Site> listModel;
    private final SitesListModelListener listener;
    private final Logger LOG = Logger.getInstance(SitesListModel.class);

    public SitesListModel(RestService restService, TaskManagerService taskManagerService, SitesListModelListener listener) {
        this.restService = restService;
        this.taskManagerService = taskManagerService;
        this.listModel = new DefaultListModel<>();
        this.listener = listener;
    }

    public ListModel<Site> getListModel() {
        return this.listModel;
    }

    private void fetch() {
        this.listener.onFetchingSites();

        this.taskManagerService.async(() -> {
            return this.restService.readSites();
        }).onSuccess(sites -> {
            this.listModel.clear();
            for (Site site : sites) {
                this.listModel.addElement(site);
            }
            this.listener.onFetchingSitesCompleted();
        }).onError(throwable -> {
            this.listModel.clear();
            if (throwable instanceof MaxymiserException) {
                listener.onFetchingSitesFailed(throwable.getMessage());
            } else {
                LOG.error("Failed to fetch sites", throwable);
                listener.onFetchingSitesFailed("Unexpected error");
            }
        });

    }

    public void init() {
        fetch();
    }
}
