/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.service.impl;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Property;
import com.oracle.maxymiser.intellij.plugin.service.ApplicationSettingsService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@State(name = "OracleMaxymiserApplicationSettings", storages = {@Storage("OracleMaxymiserApplicationSettings.xml")})
public class ApplicationSettingsServiceImpl implements ApplicationSettingsService, PersistentStateComponent<ApplicationSettingsServiceImpl> {
    @Property
    private String login;
    @Property
    private String clientId;
    @Property
    private Region region;

    @Override
    public void setRegion(Region region) {
        this.region = region;
    }

    @Override
    public Region getRegion() {
        return this.region;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return this.getSecure("password");
    }

    public void setPassword(String password) {
        this.setSecure("password", password);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return this.getSecure("clientSecret");
    }

    public void setClientSecret(String clientSecret) {
        this.setSecure("clientSecret", clientSecret);
    }

    private String getSecure(String key) {
        return PasswordSafe.getInstance().getPassword(createCredentialAttributes(key));
    }

    private void setSecure(String key, String value) {
        PasswordSafe.getInstance().setPassword(createCredentialAttributes(key), value);
    }

    @NotNull
    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("Oracle Maxymiser", key));
    }

    @Nullable
    @Override
    public ApplicationSettingsServiceImpl getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ApplicationSettingsServiceImpl state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
