/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.ThrowableComputable;
import com.oracle.maxymiser.intellij.plugin.exception.MaxymiserRestException;
import com.oracle.maxymiser.intellij.plugin.service.ApplicationSettingsService;
import com.oracle.maxymiser.intellij.plugin.service.RestService;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ApplicationSettings implements Configurable {

    private JPanel mainPanel;
    private JTextField loginTextField;
    private JPasswordField passwordTextField;
    private JPasswordField clientSecretTextField;
    private JTextField clientIdTextField;
    private JComboBox<ApplicationSettingsService.Region> regionComboBox;
    private JButton testConnectionButton;
    private JTextField proxyTextField;
    private final ProgressManager progressManager;

    private ApplicationSettingsService applicationSettingsService;
    private RestService restService;

    public ApplicationSettings(ApplicationSettingsService applicationSettingsService, RestService restService, ProgressManager progressManager) {
        super();
        this.applicationSettingsService = applicationSettingsService;
        this.restService = restService;
        this.progressManager = progressManager;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Oracle Maxymiser";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        this.testConnectionButton.addActionListener(e -> ApplicationSettings.this.testConnection());

        for (ApplicationSettingsService.Region region : ApplicationSettingsService.Region.values()) {
            this.regionComboBox.addItem(region);
        }

        return this.mainPanel;
    }

    @Override
    public boolean isModified() {
        if (!StringUtils.equals(this.loginTextField.getText(), this.applicationSettingsService.getLogin())) return true;
        if (!StringUtils.equals(new String(this.passwordTextField.getPassword()), this.applicationSettingsService.getPassword()))
            return true;
        if (!StringUtils.equals(this.clientIdTextField.getText(), this.applicationSettingsService.getClientId()))
            return true;

        if (this.regionComboBox.getSelectedItem() != this.applicationSettingsService.getRegion())
            return true;

        if (!StringUtils.equals(this.proxyTextField.getText(), this.applicationSettingsService.getProxy())) {
            return true;
        }
        return !StringUtils.equals(new String(this.clientSecretTextField.getPassword()), this.applicationSettingsService.getClientSecret());

    }

    @Override
    public void apply() throws ConfigurationException {
        this.applicationSettingsService.setLogin(this.loginTextField.getText().trim());
        this.applicationSettingsService.setPassword(new String(this.passwordTextField.getPassword()).trim());
        this.applicationSettingsService.setClientId(this.clientIdTextField.getText().trim());
        this.applicationSettingsService.setClientSecret(new String(this.clientSecretTextField.getPassword()).trim());
        this.applicationSettingsService.setRegion((ApplicationSettingsService.Region) this.regionComboBox.getSelectedItem());
        this.applicationSettingsService.setProxy(this.proxyTextField.getText().trim());
    }

    @Override
    public void reset() {
        this.loginTextField.setText(this.applicationSettingsService.getLogin());
        this.passwordTextField.setText(this.applicationSettingsService.getPassword());
        this.clientIdTextField.setText(this.applicationSettingsService.getClientId());
        this.clientSecretTextField.setText(this.applicationSettingsService.getClientSecret());
        this.regionComboBox.setSelectedItem(this.applicationSettingsService.getRegion());
        this.proxyTextField.setText(this.applicationSettingsService.getProxy());
    }

    private void testConnection() {
        try {
            this.progressManager.runProcessWithProgressSynchronously(new ThrowableComputable<Void, Exception>() {
                @Override
                public Void compute() throws Exception {
                    restService.authenticate(
                            (ApplicationSettingsService.Region) regionComboBox.getSelectedItem(),
                            loginTextField.getText().trim(),
                            new String(passwordTextField.getPassword()).trim(),
                            clientIdTextField.getText().trim(),
                            new String(clientSecretTextField.getPassword()).trim(),
                            proxyTextField.getText());

                    return null;
                }
            }, "Connecting...", false, null);

            Messages.showInfoMessage("Connection successful", "Success");
        } catch (MaxymiserRestException e) {
            Messages.showErrorDialog("Connection failed: " + e.getMessage(), "Failed");
        } catch (Exception e) {
            Messages.showErrorDialog("Connection failed: Unexpected error", "Failed");
        }

    }
}
