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
import com.oracle.maxymiser.intellij.plugin.service.PullService;
import com.oracle.maxymiser.intellij.plugin.service.RestService;
import com.oracle.maxymiser.intellij.plugin.support.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PullServiceImpl implements PullService {
    private final RestService restService;


    public PullServiceImpl(RestService restService) {
        this.restService = restService;
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

    @Override
    public PullResult pull(String baseDir, String path, PullProgress progress) throws MaxymiserException {
        try {
            String campaignId = this.readCampaignId(baseDir);

            String siteId = this.readSiteId(baseDir);

            if ((path.startsWith("Elements") || "Elements".startsWith(path)) && !progress.isCancelled()) {
                progress.setText("Pulling Variants...");
                List<CampaignElement> campaignElements = this.restService.readCampaignElements(siteId, campaignId);

                for (CampaignElement element : campaignElements) {
                    String elementPath = "Elements/" + element.getName();
                    if ((path.startsWith(elementPath) || elementPath.startsWith(path)) && !progress.isCancelled()) {
                        List<CampaignElementVariant> variants = this.restService.readCampaignElementVariants(siteId, campaignId, element.getId());
                        for (CampaignElementVariant variant : variants) {
                            if (variant.isDefault()) continue;
                            String variantPath = elementPath + "/" + variant.getName();
                            if (variantPath.startsWith(path) || path.startsWith(variantPath)) {
                                FileUtils.writeStringToFile(new File(baseDir, variantPath + ".html"), variant.getContent());
                            }
                        }
                    }
                }
            }

            if ((path.startsWith("Scripts") || "Scripts".startsWith(path)) && !progress.isCancelled()) {
                progress.setText("Pulling Scripts...");
                List<CampaignScript> campaignScripts = this.restService.readCampaignScripts(siteId, campaignId);
                for (CampaignScript campaignScript : campaignScripts) {
                    String scriptPath = "Scripts/" + campaignScript.getName();
                    if (path.startsWith(scriptPath) || scriptPath.startsWith(path)) {
                        FileUtils.writeStringToFile(new File(baseDir, scriptPath + ".js"), campaignScript.getContent());
                    }
                }
            }

            if ((path.startsWith("Actions") || "Actions".startsWith(path)) && !progress.isCancelled()) {
                progress.setText("Pulling Actions...");
                List<CampaignAction> campaignActions = this.restService.readCampaignActions(siteId, campaignId);
                for (CampaignAction campaignAction : campaignActions) {
                    if (campaignAction.isShared()) continue;
                    if ("Content_Seen".equals(campaignAction.getType())) continue;
                    String actionPath = "Actions/" + campaignAction.getName();
                    if (path.startsWith(actionPath) || actionPath.startsWith(path)) {
                        FileUtils.writeStringToFile(new File(baseDir, actionPath + ".js"), campaignAction.getScriptContent());
                    }
                }
            }

            return new PullResult();
        } catch (MaxymiserException e) {
            throw e;
        } catch (Exception e) {
            throw new MaxymiserRuntimeException("Unexpected error", e);
        }
    }

    @Override
    public String init(String campaignName, String siteId, String campaignId, String path) {
        try {
            File directory = new File(path, campaignName);
            FileUtils.forceMkdir(directory);

            this.writeSiteId(directory.getAbsolutePath(), siteId);
            this.writeCampaignId(directory.getAbsolutePath(), campaignId);

            return directory.getAbsolutePath();
        } catch (Exception e) {
            throw new MaxymiserRuntimeException("Unable to initialize campaign", e);
        }
    }

    private void writeSiteId(String baseDir, String siteId) throws IOException {
        File site = new File(baseDir, ".siteId");

        FileUtils.writeStringToFile(site, siteId);
    }

    private void writeCampaignId(String baseDir, String campaignId) throws IOException {
        File campaign = new File(baseDir, ".campaignId");

        FileUtils.writeStringToFile(campaign, campaignId);
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
