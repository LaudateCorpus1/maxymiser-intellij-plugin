/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.model;

public class CampaignElement {
    private String id;
    private String name;

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public CampaignElement setId(String id) {
        this.id = id;
        return this;
    }

    public CampaignElement setName(String name) {
        this.name = name;
        return this;
    }
}
