/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.model;

public class CampaignElementVariant {
    private String id;
    private String name;
    private String content;
    private Boolean isDefault;

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getContent() {
        return this.content;
    }

    public Boolean isDefault() {
        return this.isDefault;
    }

    public CampaignElementVariant setId(String id) {
        this.id = id;
        return this;
    }

    public CampaignElementVariant setName(String name) {
        this.name = name;
        return this;
    }

    public CampaignElementVariant setContent(String content) {
        this.content = content;
        return this;
    }

    public CampaignElementVariant setDefault(Boolean aDefault) {
        isDefault = aDefault;
        return this;
    }
}
