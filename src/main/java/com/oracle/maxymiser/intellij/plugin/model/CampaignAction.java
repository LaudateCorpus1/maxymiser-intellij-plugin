/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.model;

public class CampaignAction {
    private String id;
    private String name;
    private String scriptContent;
    private Boolean isShared;
    private String type;

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getScriptContent() {
        return this.scriptContent;
    }

    public Boolean isShared() {
        return this.isShared;
    }

    public String getType() {
        return this.type;
    }

    public CampaignAction setId(String id) {
        this.id = id;
        return this;
    }

    public CampaignAction setName(String name) {
        this.name = name;
        return this;
    }

    public CampaignAction setShared(Boolean shared) {
        isShared = shared;
        return this;
    }

    public CampaignAction setType(String type) {
        this.type = type;
        return this;
    }

    public CampaignAction setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
        return this;
    }
}
