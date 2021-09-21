module com.flowapp.petroleumeconomics {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.jetbrains.annotations;

    opens com.flowapp.petroleumeconomics to javafx.fxml;
    exports com.flowapp.petroleumeconomics;

    opens com.flowapp.petroleumeconomics.Controllers to javafx.fxml;
    exports com.flowapp.petroleumeconomics.Controllers;
}