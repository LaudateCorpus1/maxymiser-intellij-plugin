/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.service;

public interface ApplicationSettingsService {
    enum Region {
        EMEA("https://api-eu.maxymiser.com", "https://api-auth-eu.maxymiser.com"),
        US("https://api-us.maxymiser.com", "https://api-auth-us.maxymiser.com");

        private final String endpoint;
        private final String authEndpoint;

        Region(String endpoint, String authEndpoint) {
            this.endpoint = endpoint;
            this.authEndpoint = authEndpoint;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public String getAuthEndpoint() {
            return authEndpoint;
        }
    }

    Region getRegion();

    void setRegion(Region region);

    String getLogin();

    void setLogin(String login);

    String getPassword();

    void setPassword(String password);

    String getClientId();

    void setClientId(String clientId);

    String getClientSecret();

    void setClientSecret(String clientSecret);

}
