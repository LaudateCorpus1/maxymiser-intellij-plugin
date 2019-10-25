/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.service;

import com.oracle.maxymiser.intellij.plugin.exception.MaxymiserRestException;
import com.oracle.maxymiser.intellij.plugin.model.*;

import java.util.List;

public interface RestService {
    List<Site> readSites() throws MaxymiserRestException;

    List<Campaign> readCampaigns(String siteId) throws MaxymiserRestException;

    List<CampaignElement> readCampaignElements(String siteId, String campaignId) throws MaxymiserRestException;

    List<CampaignElementVariant> readCampaignElementVariants(String siteId, String campaignId, String elementId) throws MaxymiserRestException;

    List<CampaignScript> readCampaignScripts(String siteId, String campaignId) throws MaxymiserRestException;

    List<CampaignAction> readCampaignActions(String siteId, String campaignId) throws MaxymiserRestException;

    Token authenticate(ApplicationSettingsService.Region region, String login, String password, String clientId, String clientSecret, String proxy) throws MaxymiserRestException;

    void updateCampaignElementVariant(String siteId, String campaignId, String elementId, String variantId, CampaignElementVariant variant) throws MaxymiserRestException;

    void updateCampaignScript(String siteId, String campaignId, String scriptId, CampaignScript campaignScript) throws MaxymiserRestException;

    void updateCampaignAction(String siteId, String campaignId, String actionId, CampaignAction campaignAction) throws MaxymiserRestException;

    void publishToSandbox(String siteId) throws MaxymiserRestException;
}
