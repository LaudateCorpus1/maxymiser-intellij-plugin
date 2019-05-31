/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.oracle.maxymiser.intellij.plugin.exception.MaxymiserException;
import com.oracle.maxymiser.intellij.plugin.service.PullService;
import com.oracle.maxymiser.intellij.plugin.service.UIService;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PullAction extends AnAction {
    private final Logger LOG = Logger.getInstance(PullAction.class);

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
            Project project = event.getProject();

            PullService pullService = this.getPullService();
            UIService uiService = this.getUIService();

            String baseDir = pullService.resolveBaseDir(virtualFile.getPath(), project.getBasePath());
            String path = "";

            if (baseDir == null) {

                UIService.GetCampaignResult campaign = uiService.getCampaign(project);

                if (campaign == null) return;

                baseDir = pullService.init(campaign.getCampaign().getName(), campaign.getSite().getId(), campaign.getCampaign().getId(), virtualFile.getPath());
            } else {
                path = new File(baseDir).toURI().relativize(new File(virtualFile.getPath()).toURI()).getPath();
            }

            String finalPath = path;
            String finalBaseDir = baseDir;

            PullService.PullResult pullResult = this.getProgressManager().runProcessWithProgressSynchronously(new ThrowableComputable<PullService.PullResult, Exception>() {
                @Override
                public PullService.PullResult compute() throws Exception {
                    getProgressManager().getProgressIndicator().setIndeterminate(true);

                    return pullService.pull(finalBaseDir, finalPath, new PullService.PullProgress() {
                        @Override
                        public void setText(String text) {
                            getProgressManager().getProgressIndicator().setText(text);
                        }

                        @Override
                        public boolean isCancelled() {
                            return getProgressManager().getProgressIndicator().isCanceled();
                        }
                    });
                }
            }, "Pulling Campaign Data", true, project);

            virtualFile.refresh(false, true);

            FileEditorManagerEx managerEx = FileEditorManagerEx.getInstanceEx(project);
            VirtualFile[] openFiles = managerEx.getOpenFiles();

            for (VirtualFile file : openFiles) {
                managerEx.updateFilePresentation(file);
            }

        } catch (MaxymiserException e) {
            Messages.showErrorDialog(e.getMessage(), "Error");
        } catch (Exception e) {
            LOG.error("Failed to pull", e);
            Messages.showErrorDialog("Unexpected error", "Error");
        }
    }

    private PullService getPullService() {
        return ServiceManager.getService(PullService.class);
    }

    private UIService getUIService() {
        return ServiceManager.getService(UIService.class);
    }

    private ProgressManager getProgressManager() {
        return ServiceManager.getService(ProgressManager.class);
    }
}
