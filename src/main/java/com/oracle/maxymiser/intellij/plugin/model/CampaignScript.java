/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.model;

public class CampaignScript {
    private String id;
    private String name;
    private String content;

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getContent() {
        return this.content;
    }

    public CampaignScript setId(String id) {
        this.id = id;
        return this;
    }

    public CampaignScript setName(String name) {
        this.name = name;
        return this;
    }

    public CampaignScript setContent(String content) {
        this.content = content;
        return this;
    }
}
