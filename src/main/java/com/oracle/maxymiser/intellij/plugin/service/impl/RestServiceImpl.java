/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.service.impl;

import com.google.gson.Gson;
import com.oracle.maxymiser.intellij.plugin.exception.MaxymiserRestException;
import com.oracle.maxymiser.intellij.plugin.model.*;
import com.oracle.maxymiser.intellij.plugin.service.ApplicationSettingsService;
import com.oracle.maxymiser.intellij.plugin.service.RestService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class RestServiceImpl implements RestService {
    private final HttpClient httpClient;
    private final ApplicationSettingsService applicationSettingsService;
    private Token token;
    private Date tokenExpiration;

    public RestServiceImpl(ApplicationSettingsService applicationSettingsService) {
        this.applicationSettingsService = applicationSettingsService;
        this.httpClient = HttpClients.createDefault();
    }

    @Override
    public List<Site> readSites() throws MaxymiserRestException {
        return Arrays.asList(get("/v1/sites", ReadSitesResponse.class).getItems());
    }

    @Override
    public List<Campaign> readCampaigns(String siteId) throws MaxymiserRestException {
        return Arrays.asList(get(String.format("/v1/sites/%s/sandbox/campaigns", siteId), ReadCampaignsResponse.class).getItems());
    }

    @Override
    public List<CampaignElement> readCampaignElements(String siteId, String campaignId) throws MaxymiserRestException {
        return Arrays.asList(get(String.format("/v1/sites/%s/sandbox/campaigns/%s/elements", siteId, campaignId), ReadCampaignElementsResponse.class).getItems());
    }

    @Override
    public List<CampaignElementVariant> readCampaignElementVariants(String siteId, String campaignId, String elementId) throws MaxymiserRestException {
        return Arrays.asList(get(String.format("/v1/sites/%s/sandbox/campaigns/%s/elements/%s/variants", siteId, campaignId, elementId), ReadCampaignElementVariantsResponse.class).getItems());
    }

    @Override
    public List<CampaignScript> readCampaignScripts(String siteId, String campaignId) throws MaxymiserRestException {
        return Arrays.asList(get(String.format("/v1/sites/%s/sandbox/campaigns/%s/scripts", siteId, campaignId), ReadCampaignScriptsResponse.class).getItems());
    }

    @Override
    public List<CampaignAction> readCampaignActions(String siteId, String campaignId) throws MaxymiserRestException {
        return Arrays.asList(get(String.format("/v1/sites/%s/sandbox/campaigns/%s/actions", siteId, campaignId), ReadCampaignActionsResponse.class).getItems());
    }

    @Override
    public void updateCampaignElementVariant(String siteId, String campaignId, String elementId, String variantId, CampaignElementVariant variant) throws MaxymiserRestException {
        put(String.format("/v1/sites/%s/sandbox/campaigns/%s/elements/%s/variants/%s", siteId, campaignId, elementId, variantId), variant, CampaignElementVariant.class);
    }

    @Override
    public void updateCampaignScript(String siteId, String campaignId, String scriptId, CampaignScript campaignScript) throws MaxymiserRestException {
        put(String.format("/v1/sites/%s/sandbox/campaigns/%s/scripts/%s", siteId, campaignId, scriptId), campaignScript, CampaignScript.class);
    }

    @Override
    public void updateCampaignAction(String siteId, String campaignId, String actionId, CampaignAction campaignAction) throws MaxymiserRestException {
        put(String.format("/v1/sites/%s/sandbox/campaigns/%s/actions/%s", siteId, campaignId, actionId), campaignAction, CampaignAction.class);
    }

    @Override
    public void publishToSandbox(String siteId) throws MaxymiserRestException {
        put(String.format("/v1/sites/%s/sandbox/publish", siteId), null, Void.class);
    }

    private <T> T get(String uri, Class<T> clazz) throws MaxymiserRestException {
        checkSettings();

        HttpGet get = new HttpGet(String.format("%s%s", applicationSettingsService.getRegion().getEndpoint(), uri));

        return this.execute(get, clazz);
    }

    private <T> T put(String uri, Object body, Class<T> clazz) throws MaxymiserRestException {
        checkSettings();

        try {
            HttpPut put = new HttpPut(String.format("%s%s", applicationSettingsService.getRegion().getEndpoint(), uri));

            if (body != null) {
                put.setEntity(new StringEntity(new Gson().toJson(body)));
                put.addHeader("Content-Type", "application/json");
            }

            return this.execute(put, clazz);
        } catch (MaxymiserRestException e) {
            throw e;
        } catch (Exception e) {
            throw new MaxymiserRestException("Unexpected error", e);
        }
    }

    private <T> T execute(HttpRequestBase request, Class<T> clazz) throws MaxymiserRestException {
        HttpResponse response = null;
        try {
            refreshTokenIfRequired();

            request.addHeader("Authorization", String.format("%s %s", this.token.getTokenType(), this.token.getAccessToken()));

            request.setConfig(this.createRequestConfig(applicationSettingsService.getProxy()));

            response = this.httpClient.execute(request);

            checkResponse(response);

            T result = new Gson().fromJson(new BufferedReader(new InputStreamReader(response.getEntity().getContent())), clazz);

            EntityUtils.consume(response.getEntity());

            return result;
        } catch (MaxymiserRestException e) {
            throw e;
        } catch (Exception e) {
            throw new MaxymiserRestException("Unexpected error", e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    // NOOP
                }
            }

        }
    }

    @Override
    public Token authenticate(ApplicationSettingsService.Region region, String login, String password, String clientId, String clientSecret, String proxy) throws MaxymiserRestException {
        HttpResponse response = null;
        try {
            HttpPost post = new HttpPost(region.getAuthEndpoint() + "/oauth2/v1/tokens");
            post.setEntity(new UrlEncodedFormEntity(Arrays.asList(
                    new BasicNameValuePair("grant_type", "password"),
                    new BasicNameValuePair("username", login),
                    new BasicNameValuePair("password", password)
            )));

            post.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            post.addHeader(new BasicScheme().authenticate(new UsernamePasswordCredentials(clientId, clientSecret), post, null));

            post.setConfig(this.createRequestConfig(proxy));

            response = httpClient.execute(post);

            checkResponse(response);

            return new Gson().fromJson(new BufferedReader(new InputStreamReader(response.getEntity().getContent())), Token.class);
        } catch (MaxymiserRestException e) {
            throw new MaxymiserRestException("Authentication error", e);
        } catch (HttpHostConnectException e) {
            throw new MaxymiserRestException("Unable to connect", e);
        } catch (Exception e) {
            throw new MaxymiserRestException("Unexpected error", e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    // NOOP
                }
            }
        }
    }

    private RequestConfig createRequestConfig(String proxy) throws URISyntaxException {
        RequestConfig.Builder builder = RequestConfig.copy(RequestConfig.DEFAULT);

        if (StringUtils.isNotBlank(proxy)) {
            URI proxyUri = new URI(proxy);
            builder.setProxy(new HttpHost(proxyUri.getHost(), proxyUri.getPort(), proxyUri.getScheme()));
        }

        builder.setConnectionRequestTimeout(10 * 1000);
        builder.setConnectTimeout(10 * 1000);
        builder.setSocketTimeout(10 * 1000);

        return builder.build();
    }

    private void checkResponse(HttpResponse response) throws MaxymiserRestException {
        if (response.getStatusLine().getStatusCode() != 200) {
            try {
                ErrorResponse errorResponse = new Gson().fromJson(new BufferedReader(new InputStreamReader(response.getEntity().getContent())), ErrorResponse.class);
                throw new MaxymiserRestException("Request failed", errorResponse);
            } catch (IOException e) {
                throw new MaxymiserRestException("Unexpected error", e);
            }
        }
    }

    private void checkSettings() throws MaxymiserRestException {
        if (applicationSettingsService.getRegion() == null) {
            throw new MaxymiserRestException("Configuration error - Region is not set");
        }

        if (StringUtils.isBlank(applicationSettingsService.getLogin())) {
            throw new MaxymiserRestException("Configuration error - Login is not set");
        }

        if (StringUtils.isBlank(applicationSettingsService.getPassword())) {
            throw new MaxymiserRestException("Configuration error - Password is not set");
        }

        if (StringUtils.isBlank(applicationSettingsService.getClientId())) {
            throw new MaxymiserRestException("Configuration error - Client ID is not set");
        }

        if (StringUtils.isBlank(applicationSettingsService.getClientSecret())) {
            throw new MaxymiserRestException("Configuration error - Client Secret is not set");
        }
    }

    private synchronized void refreshTokenIfRequired() throws MaxymiserRestException {
        if (this.token != null && this.tokenExpiration.after(new Date())) {
            return;
        }

        this.token = this.authenticate(
                this.applicationSettingsService.getRegion(),
                this.applicationSettingsService.getLogin(),
                this.applicationSettingsService.getPassword(),
                this.applicationSettingsService.getClientId(),
                this.applicationSettingsService.getClientSecret(),
                this.applicationSettingsService.getProxy()
        );

        this.tokenExpiration = DateUtils.addSeconds(new Date(), this.token.getExpiresIn() / 2);
    }

}
