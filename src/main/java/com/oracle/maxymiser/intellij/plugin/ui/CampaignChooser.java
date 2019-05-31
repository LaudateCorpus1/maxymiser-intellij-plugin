/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.ui;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLoadingPanel;
import com.oracle.maxymiser.intellij.plugin.model.Campaign;
import com.oracle.maxymiser.intellij.plugin.model.Site;
import com.oracle.maxymiser.intellij.plugin.service.RestService;
import com.oracle.maxymiser.intellij.plugin.service.TaskManagerService;
import com.oracle.maxymiser.intellij.plugin.ui.support.CampaignsListModel;
import com.oracle.maxymiser.intellij.plugin.ui.support.CampaignsListModelListener;
import com.oracle.maxymiser.intellij.plugin.ui.support.SitesListModel;
import com.oracle.maxymiser.intellij.plugin.ui.support.SitesListModelListener;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CampaignChooser extends DialogWrapper implements SitesListModelListener, CampaignsListModelListener {
    private JPanel mainPanel;
    private JList<Site> sitesList;
    private JList<Campaign> campaignsList;
    private JBLoadingPanel loadingPanel;
    private final RestService restService;
    private final TaskManagerService taskManagerService;

    public CampaignChooser(RestService restService, TaskManagerService taskManagerService, @Nullable Project project) {
        super(project);
        this.restService = restService;
        this.taskManagerService = taskManagerService;
        init();
        setTitle("Choose campaign");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        SitesListModel sitesListModel = new SitesListModel(this.restService, this.taskManagerService, this);

        this.sitesList.setModel(sitesListModel.getListModel());
        this.sitesList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Site site = (Site) value;
                label.setText(site.getName());
                return label;
            }
        });

        CampaignsListModel campaignsListModel = new CampaignsListModel(this.restService, this.taskManagerService, this);

        this.campaignsList.setModel(campaignsListModel.getListModel());
        this.campaignsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                Campaign campaign = (Campaign) value;
                label.setText(campaign.getName());

                return label;
            }
        });

        this.sitesList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            campaignsListModel.setSite(sitesList.getSelectedValue());
            refreshUI();
        });

        this.campaignsList.addListSelectionListener(e -> refreshUI());

        this.loadingPanel = new JBLoadingPanel(new BorderLayout(), getDisposable());
        this.loadingPanel.add(this.mainPanel, BorderLayout.CENTER);
        this.loadingPanel.setLoadingText("Loading...");

        refreshUI();

        sitesListModel.init();
        campaignsListModel.init();

        return this.loadingPanel;
    }

    private void refreshUI() {
        setOKActionEnabled(campaignsList.getSelectedValue() != null);
    }

    @Override
    public void onFetchingSites() {
        onUIThread(() -> {
            this.setEnabled(false);
            this.loadingPanel.startLoading();
        });
    }

    @Override
    public void onFetchingSitesCompleted() {
        onUIThread(() -> {
            this.setEnabled(true);
            this.loadingPanel.stopLoading();
        });
    }

    @Override
    public void onFetchingSitesFailed(String message) {
        onUIThread(() -> {
            setEnabled(true);
            loadingPanel.stopLoading();
            Messages.showErrorDialog(message, "Error");
        });
    }

    @Override
    public void onFetchingCampaigns() {
        onUIThread(() -> {
            this.setEnabled(false);
            this.loadingPanel.startLoading();
        });
    }

    @Override
    public void onFetchingCampaignsCompleted() {
        onUIThread(() -> {
            this.setEnabled(true);
            this.loadingPanel.stopLoading();
        });
    }

    @Override
    public void onFetchingCampaignsFailed(String message) {
        onUIThread(() -> {
            setEnabled(true);
            loadingPanel.stopLoading();
            Messages.showErrorDialog(message, "Error");
        });
    }

    private void setEnabled(boolean enabled) {
        this.setEnabled(this.mainPanel, enabled);
    }

    private void setEnabled(Component component, boolean enabled) {
        component.setEnabled(enabled);

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setEnabled(child, enabled);
            }
        }
    }

    public Campaign getCampaign() {
        return this.campaignsList.getSelectedValue();
    }

    public Site getSite() {
        return this.sitesList.getSelectedValue();
    }


    private void onUIThread(Runnable runnable) {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            runnable.run();
        } else {
            application.invokeLater(runnable, ModalityState.stateForComponent(this.mainPanel));
        }

    }

}
