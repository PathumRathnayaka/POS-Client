module com.qaldrin.posclient {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires okhttp3;
    requires com.google.gson;
    requires com.jfoenix;
    requires java.prefs; // Add this line

    opens com.qaldrin.posclient to javafx.fxml;

    exports com.qaldrin.posclient;
    exports com.qaldrin.posclient.controller;
    exports com.qaldrin.posclient.model;

    opens com.qaldrin.posclient.controller to javafx.fxml;

    exports com.qaldrin.posclient.dto;

    opens com.qaldrin.posclient.dto to com.google.gson;
    opens com.qaldrin.posclient.service to com.google.gson;

    exports com.qaldrin.posclient.util;
}