module com.qaldrin.posclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.qaldrin.posclient to javafx.fxml;
    exports com.qaldrin.posclient;
}