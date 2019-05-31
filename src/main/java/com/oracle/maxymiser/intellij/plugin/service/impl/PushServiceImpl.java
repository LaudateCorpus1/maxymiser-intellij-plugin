/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.service.impl;

import com.oracle.maxymiser.intellij.plugin.exception.MaxymiserException;
import com.oracle.maxymiser.intellij.plugin.exception.MaxymiserRuntimeException;
import com.oracle.maxymiser.intellij.plugin.model.CampaignAction;
import com.oracle.maxymiser.intellij.plugin.model.CampaignElement;
import com.oracle.maxymiser.intellij.plugin.model.CampaignElementVariant;
import com.oracle.maxymiser.intellij.plugin.model.CampaignScript;
import com.oracle.maxymiser.intellij.plugin.service.PushService;
import com.oracle.maxymiser.intellij.plugin.service.RestService;
import com.oracle.maxymiser.intellij.plugin.support.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PushServiceImpl implements PushService {
    private final RestService restService;

    public PushServiceImpl(RestService restService) {
        this.restService = restService;
    }


    @Override
    public PushResult push(String baseDir, String path, PushProgress progress) throws MaxymiserException {
        try {
            PushResult result = new PushResult();

            String campaignId = this.readCampaignId(baseDir);
            String siteId = this.readSiteId(baseDir);

            List<String> selected = this.listFiles(baseDir, new File(baseDir, path).getAbsolutePath());

            if (selected.size() == 0) return result;

            if (this.startsWith(selected, "Elements") && !progress.isCancelled()) {
                progress.setText("Pushing Variants...");
                List<CampaignElement> campaignElements = this.restService.readCampaignElements(siteId, campaignId);
                for (CampaignElement campaignElement : campaignElements) {
                    String elementPath = "Elements/" + campaignElement.getName();
                    if (this.startsWith(selected, elementPath) && !progress.isCancelled()) {
                        List<CampaignElementVariant> variants = this.restService.readCampaignElementVariants(siteId, campaignId, campaignElement.getId());
                        for (CampaignElementVariant variant : variants) {
                            if (variant.isDefault()) continue;
                            String variantPath = elementPath + "/" + variant.getName();
                            if (this.startsWith(selected, variantPath) && !progress.isCancelled()) {
                                File file = new File(baseDir, variantPath + ".html");
                                progress.setText2(file.getAbsolutePath());
                                this.restService.updateCampaignElementVariant(siteId, campaignId, campaignElement.getId(), variant.getId(), new CampaignElementVariant().setContent(FileUtils.readFileToString(file)));
                                progress.setText2(null);
                            }
                        }
                    }
                }
            }

            if (this.startsWith(selected, "Scripts") && !progress.isCancelled()) {
                progress.setText("Pushing Scripts...");
                List<CampaignScript> campaignScripts = this.restService.readCampaignScripts(siteId, campaignId);
                for (CampaignScript campaignScript : campaignScripts) {
                    String scriptPath = "Scripts/" + campaignScript.getName();
                    if (this.startsWith(selected, scriptPath) && !progress.isCancelled()) {
                        File file = new File(baseDir, scriptPath + ".js");
                        progress.setText2(file.getAbsolutePath());
                        this.restService.updateCampaignScript(siteId, campaignId, campaignScript.getId(), new CampaignScript().setContent(FileUtils.readFileToString(file)));
                        progress.setText2(null);
                    }
                }
            }

            if (this.startsWith(selected, "Actions") && !progress.isCancelled()) {
                progress.setText("Pushing Actions...");
                List<CampaignAction> campaignActions = this.restService.readCampaignActions(siteId, campaignId);
                for (CampaignAction campaignAction : campaignActions) {
                    if (campaignAction.isShared()) continue;
                    if ("Content_Seen".equals(campaignAction.getType())) continue;
                    String actionPath = "Actions/" + campaignAction.getName();
                    if (this.startsWith(selected, actionPath) && !progress.isCancelled()) {
                        File file = new File(baseDir, actionPath + ".js");
                        progress.setText2(file.getAbsolutePath());
                        this.restService.updateCampaignAction(siteId, campaignId, campaignAction.getId(), new CampaignAction().setScriptContent(FileUtils.readFileToString(file)));
                        progress.setText2(null);
                    }
                }
            }

            progress.setText("Publishing to Sandbox...");

            this.restService.publishToSandbox(siteId);

            return result;

        } catch (MaxymiserException e) {
            throw e;
        } catch (Exception e) {
            throw new MaxymiserRuntimeException("Unexpected error", e);
        }
    }

    private boolean startsWith(List<String> list, String prefix) {
        for (String item : list) {
            if (item.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private List<String> listFiles(String baseDir, String path) {
        List<String> result = new LinkedList<>();

        listFiles(result, baseDir, path);

        return result;
    }

    private void listFiles(List<String> list, String baseDir, String path) {
        File file = new File(path);

        if (file.isDirectory()) {
            for (String child : file.list()) {
                listFiles(list, baseDir, new File(file, child).getAbsolutePath());
            }

        } else {
            list.add(new File(baseDir).toURI().relativize(new File(path).toURI()).getPath());
        }
    }


    @Override
    public String resolveBaseDir(String path, String root) {
        File file = new File(path);

        while (!file.getAbsolutePath().equals(new File(root).getAbsolutePath())) {
            if (file.isDirectory() && new File(file, ".siteId").exists() && new File(file, ".campaignId").exists()) {
                return file.getAbsolutePath();
            }
            file = file.getParentFile();
        }

        return null;
    }

    private String readSiteId(String baseDir) throws IOException {
        File campaign = new File(baseDir, ".siteId");

        return FileUtils.readFileToString(campaign).trim();

    }

    private String readCampaignId(String baseDir) throws IOException {
        File campaign = new File(baseDir, ".campaignId");

        return FileUtils.readFileToString(campaign).trim();
    }
}
