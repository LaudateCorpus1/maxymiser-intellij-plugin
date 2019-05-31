/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.service.impl;

import com.oracle.maxymiser.intellij.plugin.exception.MaxymiserException;
import com.oracle.maxymiser.intellij.plugin.model.CampaignAction;
import com.oracle.maxymiser.intellij.plugin.model.CampaignElement;
import com.oracle.maxymiser.intellij.plugin.model.CampaignElementVariant;
import com.oracle.maxymiser.intellij.plugin.model.CampaignScript;
import com.oracle.maxymiser.intellij.plugin.service.PushService;
import com.oracle.maxymiser.intellij.plugin.service.RestService;
import com.oracle.maxymiser.intellij.plugin.support.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class PushServiceImplTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void shouldResolveBasePath() throws IOException {
        PushService pushService = new PushServiceImpl(Mockito.mock(RestService.class));

        File campaign = tmpFolder.newFolder("Campaign");
        tmpFolder.newFolder("Campaign", "Elements");
        tmpFolder.newFile("Campaign/.siteId");
        tmpFolder.newFile("Campaign/.campaignId");

        assertEquals(campaign.getAbsolutePath(), pushService.resolveBaseDir(new File(tmpFolder.getRoot(), "Campaign/Elements").getAbsolutePath(), tmpFolder.getRoot().getAbsolutePath()));
    }

    @Test
    public void shouldPushVariants() throws IOException, MaxymiserException {
        RestService restService = Mockito.mock(RestService.class);
        PushService pushService = new PushServiceImpl(restService);

        File campaign = tmpFolder.newFolder("Campaign");
        tmpFolder.newFolder("Campaign", "Elements");
        FileUtils.writeStringToFile(tmpFolder.newFile("Campaign/.siteId"), "siteId");
        FileUtils.writeStringToFile(tmpFolder.newFile("Campaign/.campaignId"), "campaignId");
        tmpFolder.newFolder("Campaign", "Elements", "Element1");
        FileUtils.writeStringToFile(tmpFolder.newFile("Campaign/Elements/Element1/Variant1.html"), "New content");

        Mockito.when(restService.readCampaignElements("siteId", "campaignId")).thenReturn(Arrays.asList(
                new CampaignElement().setId("elementId1").setName("Element1")
        ));

        Mockito.when(restService.readCampaignElementVariants("siteId", "campaignId", "elementId1")).thenReturn(Arrays.asList(
                new CampaignElementVariant().setId("variantId1").setName("Default").setContent("").setDefault(true),
                new CampaignElementVariant().setId("variantId2").setName("Variant1").setContent("Content").setDefault(false)
        ));

        pushService.push(campaign.getAbsolutePath(), "Elements", Mockito.mock(PushService.PushProgress.class));

        Mockito.verify(restService, Mockito.times(1)).updateCampaignElementVariant(any(), any(), any(), any(), any());
        Mockito.verify(restService).updateCampaignElementVariant(eq("siteId"), eq("campaignId"), eq("elementId1"), eq("variantId2"), Mockito.argThat(argument -> "New content".equals(argument.getContent())));
    }

    @Test
    public void shouldPushScripts() throws IOException, MaxymiserException {
        RestService restService = Mockito.mock(RestService.class);
        PushService pushService = new PushServiceImpl(restService);

        File campaign = tmpFolder.newFolder("Campaign");
        tmpFolder.newFolder("Campaign", "Scripts");
        FileUtils.writeStringToFile(tmpFolder.newFile("Campaign/.siteId"), "siteId");
        FileUtils.writeStringToFile(tmpFolder.newFile("Campaign/.campaignId"), "campaignId");
        FileUtils.writeStringToFile(tmpFolder.newFile("Campaign/Scripts/Rendering.js"), "New content");

        Mockito.when(restService.readCampaignScripts("siteId", "campaignId")).thenReturn(Arrays.asList(
                new CampaignScript().setId("scriptId1").setName("Rendering").setContent("Content")
        ));

        pushService.push(campaign.getAbsolutePath(), "Scripts", Mockito.mock(PushService.PushProgress.class));

        Mockito.verify(restService, Mockito.times(1)).updateCampaignScript(any(), any(), any(), any());
        Mockito.verify(restService).updateCampaignScript(eq("siteId"), eq("campaignId"), eq("scriptId1"), Mockito.argThat(argument -> "New content".equals(argument.getContent())));
    }

    @Test
    public void shouldPushActions() throws IOException, MaxymiserException {
        RestService restService = Mockito.mock(RestService.class);
        PushService pushService = new PushServiceImpl(restService);

        File campaign = tmpFolder.newFolder("Campaign");
        tmpFolder.newFolder("Campaign", "Actions");
        FileUtils.writeStringToFile(tmpFolder.newFile("Campaign/.siteId"), "siteId");
        FileUtils.writeStringToFile(tmpFolder.newFile("Campaign/.campaignId"), "campaignId");
        FileUtils.writeStringToFile(tmpFolder.newFile("Campaign/Actions/Click Action.js"), "New content");

        Mockito.when(restService.readCampaignActions("siteId", "campaignId")).thenReturn(Arrays.asList(
                new CampaignAction().setId("actionId1").setName("Click Action").setShared(false).setType("Click_Through").setScriptContent("Content")
        ));

        pushService.push(campaign.getAbsolutePath(), "Actions", Mockito.mock(PushService.PushProgress.class));

        Mockito.verify(restService, Mockito.times(1)).updateCampaignAction(any(), any(), any(), any());
        Mockito.verify(restService).updateCampaignAction(eq("siteId"), eq("campaignId"), eq("actionId1"), Mockito.argThat(argument -> "New content".equals(argument.getScriptContent())));
    }


}
