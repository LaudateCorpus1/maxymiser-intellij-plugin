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
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.oracle.maxymiser.intellij.plugin.exception.MaxymiserException;
import com.oracle.maxymiser.intellij.plugin.service.PushService;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PushAction extends AnAction {
    private final Logger LOG = Logger.getInstance(PushAction.class);

    @Override
    public void update(@NotNull AnActionEvent event) {
        VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        Project project = event.getProject();
        event.getPresentation().setEnabled(virtualFile != null && project != null && getPushService().resolveBaseDir(virtualFile.getPath(), project.getBasePath()) != null);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        try {
            VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
            Project project = event.getProject();

            PushService pushService = this.getPushService();

            String baseDir = pushService.resolveBaseDir(virtualFile.getPath(), project.getBasePath());
            String path = new File(baseDir).toURI().relativize(new File(virtualFile.getPath()).toURI()).getPath();

            String finalPath = path;
            String finalBaseDir = baseDir;

            PushService.PushResult pushResult = this.getProgressManager().runProcessWithProgressSynchronously(new ThrowableComputable<PushService.PushResult, Exception>() {
                @Override
                public PushService.PushResult compute() throws Exception {
                    getProgressManager().getProgressIndicator().setIndeterminate(true);

                    return pushService.push(finalBaseDir, finalPath, new PushService.PushProgress() {
                        @Override
                        public void setText(String text) {
                            getProgressManager().getProgressIndicator().setText(text);
                        }

                        @Override
                        public void setText2(String text) {
                            getProgressManager().getProgressIndicator().setText2(text);
                        }

                        @Override
                        public boolean isCancelled() {
                            return getProgressManager().getProgressIndicator().isCanceled();
                        }
                    });
                }
            }, "Pushing Campaign Data", true, project);

        } catch (MaxymiserException e) {
            Messages.showErrorDialog(e.getMessage(), "Error");
        } catch (Exception e) {
            LOG.error("Failed to push", e);
            Messages.showErrorDialog("Unexpected error", "Error");
        }
    }

    private PushService getPushService() {
        return ServiceManager.getService(PushService.class);
    }

    private ProgressManager getProgressManager() {
        return ServiceManager.getService(ProgressManager.class);
    }
}
