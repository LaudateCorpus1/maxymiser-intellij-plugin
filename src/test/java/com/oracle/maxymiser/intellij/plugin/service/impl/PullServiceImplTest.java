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
import com.oracle.maxymiser.intellij.plugin.service.PullService;
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
import static org.junit.Assert.assertTrue;

public class PullServiceImplTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void shouldInitCampaign() throws MaxymiserException, IOException {
        PullService pullService = new PullServiceImpl(Mockito.mock(RestService.class));

        pullService.init("Campaign", "siteId", "campaignId", tmpFolder.getRoot().getAbsolutePath());

        File campaignDir = new File(tmpFolder.getRoot(), "Campaign");
        assertTrue(campaignDir.isDirectory());
        assertEquals("siteId", FileUtils.readFileToString(new File(campaignDir, ".siteId")).trim());
        assertEquals("campaignId", FileUtils.readFileToString(new File(campaignDir, ".campaignId")).trim());
    }

    @Test
    public void shouldResolveBasePath() throws IOException {
        PullService pullService = new PullServiceImpl(Mockito.mock(RestService.class));

        File campaign = tmpFolder.newFolder("Campaign");
        tmpFolder.newFolder("Campaign", "Elements");
        tmpFolder.newFile("Campaign/.siteId");
        tmpFolder.newFile("Campaign/.campaignId");

        assertEquals(campaign.getAbsolutePath(), pullService.resolveBaseDir(new File(tmpFolder.getRoot(), "Campaign/Elements").getAbsolutePath(), tmpFolder.getRoot().getAbsolutePath()));
    }

    @Test
    public void shouldPullCampaignVariants() throws IOException, MaxymiserException {
        File siteId = tmpFolder.newFile(".siteId");
        FileUtils.writeStringToFile(siteId, "siteId");
        File campaignId = tmpFolder.newFile(".campaignId");
        FileUtils.writeStringToFile(campaignId, "campaignId");

        RestService restService = Mockito.mock(RestService.class);
        Mockito.when(restService.readCampaignElements("siteId", "campaignId")).thenReturn(Arrays.asList(
                new CampaignElement().setId("elementId1").setName("Element1"),
                new CampaignElement().setId("elementId2").setName("Element2")
        ));

        Mockito.when(restService.readCampaignElementVariants("siteId", "campaignId", "elementId1")).thenReturn(Arrays.asList(
                new CampaignElementVariant().setId("variantId1").setName("Default").setContent("").setDefault(true),
                new CampaignElementVariant().setId("variantId2").setName("Variant1").setContent("Variant1 content").setDefault(false)
        ));

        Mockito.when(restService.readCampaignElementVariants("siteId", "campaignId", "elementId2")).thenReturn(Arrays.asList(
                new CampaignElementVariant().setId("variantId1").setName("Default").setContent("").setDefault(true),
                new CampaignElementVariant().setId("variantId2").setName("Variant1").setContent("Variant1 content").setDefault(false)
        ));


        PullService pullService = new PullServiceImpl(restService);

        PullService.PullProgress progress = Mockito.mock(PullService.PullProgress.class);
        pullService.pull(tmpFolder.getRoot().getAbsolutePath(), "", progress);

        assertEquals(1, new File(tmpFolder.getRoot(), "Elements/Element1").listFiles().length);
        assertEquals(1, new File(tmpFolder.getRoot(), "Elements/Element2").listFiles().length);

        assertEquals("Variant1 content", FileUtils.readFileToString(new File(tmpFolder.getRoot(), "Elements/Element1/Variant1.html")).trim());
        assertEquals("Variant1 content", FileUtils.readFileToString(new File(tmpFolder.getRoot(), "Elements/Element2/Variant1.html")).trim());
    }


    @Test
    public void shouldPullCampaignScripts() throws IOException, MaxymiserException {
        File siteId = tmpFolder.newFile(".siteId");
        FileUtils.writeStringToFile(siteId, "siteId");
        File campaignId = tmpFolder.newFile(".campaignId");
        FileUtils.writeStringToFile(campaignId, "campaignId");

        RestService restService = Mockito.mock(RestService.class);
        Mockito.when(restService.readCampaignScripts("siteId", "campaignId")).thenReturn(Arrays.asList(
                new CampaignScript().setId("scriptId1").setName("Rendering").setContent("Rendering content"),
                new CampaignScript().setId("scriptId2").setName("Qualifying").setContent("Qualifying content")
        ));

        PullService pullService = new PullServiceImpl(restService);

        PullService.PullProgress progress = Mockito.mock(PullService.PullProgress.class);
        pullService.pull(tmpFolder.getRoot().getAbsolutePath(), "", progress);

        assertEquals(2, new File(tmpFolder.getRoot(), "Scripts").listFiles().length);
        assertEquals("Rendering content", FileUtils.readFileToString(new File(tmpFolder.getRoot(), "Scripts/Rendering.js")).trim());
        assertEquals("Qualifying content", FileUtils.readFileToString(new File(tmpFolder.getRoot(), "Scripts/Qualifying.js")).trim());
    }


    @Test
    public void shouldPullCampaignActions() throws IOException, MaxymiserException {
        File siteId = tmpFolder.newFile(".siteId");
        FileUtils.writeStringToFile(siteId, "siteId");
        File campaignId = tmpFolder.newFile(".campaignId");
        FileUtils.writeStringToFile(campaignId, "campaignId");

        RestService restService = Mockito.mock(RestService.class);
        Mockito.when(restService.readCampaignActions("siteId", "campaignId")).thenReturn(Arrays.asList(
                new CampaignAction().setId("actionId1").setName("Shared").setShared(true).setScriptContent("").setType("Page_Impressions"),
                new CampaignAction().setId("actionId2").setName("Content Seen").setShared(false).setScriptContent("").setType("Content_Seen"),
                new CampaignAction().setId("actionId3").setName("Click Action").setShared(false).setScriptContent("Click Action content").setType("Click_Through")
        ));

        PullService pullService = new PullServiceImpl(restService);

        PullService.PullProgress progress = Mockito.mock(PullService.PullProgress.class);
        pullService.pull(tmpFolder.getRoot().getAbsolutePath(), "", progress);

        assertEquals(1, new File(tmpFolder.getRoot(), "Actions").listFiles().length);
        assertEquals("Click Action content", FileUtils.readFileToString(new File(tmpFolder.getRoot(), "Actions/Click Action.js")).trim());
    }

}
