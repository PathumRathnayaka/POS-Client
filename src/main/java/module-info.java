module com.qaldrin.posclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires com.google.gson;


    opens com.qaldrin.posclient to javafx.fxml;
    exports com.qaldrin.posclient;
    exports com.qaldrin.posclient.controller;
    opens com.qaldrin.posclient.controller to javafx.fxml;
}