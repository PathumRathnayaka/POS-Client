package com.qaldrin.posclient.controller;

import com.qaldrin.posclient.service.ApiService;
import com.qaldrin.posclient.util.ApiConfig;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class SettingFormController implements Initializable {

    @FXML private TextField ipAddressField;
    @FXML private TextField portField;
    @FXML private Button saveButton;
    @FXML private Button testConnectionButton;
    @FXML private Label statusLabel;
    @FXML private Label currentUrlLabel;

    private final ApiService apiService = new ApiService();
    private final Preferences prefs = Preferences.userNodeForPackage(SettingFormController.class);

    // Keys for storing preferences
    private static final String PREF_IP_ADDRESS = "server_ip_address";
    private static final String PREF_PORT = "server_port";
    private static final String DEFAULT_IP = "192.168.1.100";
    private static final String DEFAULT_PORT = "8080";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadSavedSettings();
        updateCurrentUrlLabel();
    }

    /**
     * Load saved settings from preferences
     */
    private void loadSavedSettings() {
        String savedIp = prefs.get(PREF_IP_ADDRESS, DEFAULT_IP);
        String savedPort = prefs.get(PREF_PORT, DEFAULT_PORT);

        ipAddressField.setText(savedIp);
        portField.setText(savedPort);
    }

    /**
     * Update the current URL label to show active configuration
     */
    private void updateCurrentUrlLabel() {
        String currentUrl = ApiConfig.getBaseUrl();
        currentUrlLabel.setText("Current Server URL: " + currentUrl);
    }

    /**
     * Save settings when user clicks Save button
     */
    @FXML
    private void onSaveSettings() {
        String ipAddress = ipAddressField.getText().trim();
        String port = portField.getText().trim();

        // Validate input
        if (ipAddress.isEmpty()) {
            showStatus("IP Address cannot be empty!", Color.RED);
            return;
        }

        if (port.isEmpty()) {
            showStatus("Port number cannot be empty!", Color.RED);
            return;
        }

        // Validate IP address format (basic validation)
        if (!isValidIpAddress(ipAddress)) {
            showStatus("Invalid IP address format! Use format: xxx.xxx.xxx.xxx", Color.RED);
            return;
        }

        // Validate port number
        if (!isValidPort(port)) {
            showStatus("Invalid port number! Must be between 1 and 65535", Color.RED);
            return;
        }

        // Construct new base URL
        String newBaseUrl = "http://" + ipAddress + ":" + port;

        // Save to preferences
        prefs.put(PREF_IP_ADDRESS, ipAddress);
        prefs.put(PREF_PORT, port);

        // Update ApiConfig
        ApiConfig.setBaseUrl(newBaseUrl);

        // Update display
        updateCurrentUrlLabel();

        showStatus("Settings saved successfully! New URL: " + newBaseUrl, Color.GREEN);
        System.out.println("Server URL updated to: " + newBaseUrl);

        // Show confirmation dialog
        showAlert(Alert.AlertType.INFORMATION, "Settings Saved",
                "Server settings have been updated!\n\n" +
                        "IP Address: " + ipAddress + "\n" +
                        "Port: " + port + "\n\n" +
                        "Click 'Test Connection' to verify the connection.");
    }

    /**
     * Test connection to the server
     */
    @FXML
    private void onTestConnection() {
        String ipAddress = ipAddressField.getText().trim();
        String port = portField.getText().trim();

        if (ipAddress.isEmpty() || port.isEmpty()) {
            showStatus("Please enter IP address and port first!", Color.RED);
            return;
        }

        // Temporarily update the URL for testing
        String testUrl = "http://" + ipAddress + ":" + port;
        String originalUrl = ApiConfig.getBaseUrl();
        ApiConfig.setBaseUrl(testUrl);

        showStatus("Testing connection...", Color.BLUE);
        testConnectionButton.setDisable(true);

        // Test connection in background thread
        new Thread(() -> {
            try {
                boolean connected = apiService.testConnection();

                Platform.runLater(() -> {
                    testConnectionButton.setDisable(false);
                    if (connected) {
                        showStatus("✓ Connection successful!", Color.GREEN);
                        showAlert(Alert.AlertType.INFORMATION, "Connection Successful",
                                "Successfully connected to server at " + testUrl);
                    } else {
                        showStatus("✗ Connection failed! Check IP address and port.", Color.RED);
                        showAlert(Alert.AlertType.ERROR, "Connection Failed",
                                "Failed to connect to server at " + testUrl + "\n\n" +
                                        "Please check:\n" +
                                        "1. IP address is correct\n" +
                                        "2. Port number is correct\n" +
                                        "3. Server is running\n" +
                                        "4. Firewall is not blocking the connection");
                        // Restore original URL if test failed
                        ApiConfig.setBaseUrl(originalUrl);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    testConnectionButton.setDisable(false);
                    showStatus("✗ Connection error: " + e.getMessage(), Color.RED);
                    showAlert(Alert.AlertType.ERROR, "Connection Error",
                            "Error testing connection: " + e.getMessage());
                    // Restore original URL if test failed
                    ApiConfig.setBaseUrl(originalUrl);
                });
            }
        }).start();
    }

    /**
     * Reset to default settings
     */
    @FXML
    private void onResetToDefaults() {
        boolean confirmed = showConfirmation("Reset Settings",
                "Are you sure you want to reset to default settings?\n\n" +
                        "Default IP: " + DEFAULT_IP + "\n" +
                        "Default Port: " + DEFAULT_PORT);

        if (confirmed) {
            ipAddressField.setText(DEFAULT_IP);
            portField.setText(DEFAULT_PORT);

            // Save defaults
            prefs.put(PREF_IP_ADDRESS, DEFAULT_IP);
            prefs.put(PREF_PORT, DEFAULT_PORT);

            String defaultUrl = "http://" + DEFAULT_IP + ":" + DEFAULT_PORT;
            ApiConfig.setBaseUrl(defaultUrl);
            updateCurrentUrlLabel();

            showStatus("Settings reset to defaults", Color.BLUE);
        }
    }

    /**
     * Validate IP address format
     */
    private boolean isValidIpAddress(String ip) {
        // Allow localhost
        if (ip.equalsIgnoreCase("localhost")) {
            return true;
        }

        // Basic IP validation (xxx.xxx.xxx.xxx)
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(ipPattern);
    }

    /**
     * Validate port number
     */
    private boolean isValidPort(String port) {
        try {
            int portNum = Integer.parseInt(port);
            return portNum >= 1 && portNum <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Show status message with color
     */
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);
    }

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show confirmation dialog
     */
    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }
}