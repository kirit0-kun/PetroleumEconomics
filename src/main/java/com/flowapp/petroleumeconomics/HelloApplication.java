package com.flowapp.petroleumeconomics;

import com.flowapp.petroleumeconomics.Services.EconomicsCalculator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
//        stage.show();

//        // Project B
//        new EconomicsCalculator().calculate(500,
//                5_000_000,
//                25_000_000,
//                20_000_000,
//                60,
//                0.98,
//                0.07,
//                10,
//                4,
//                1_500_000,
//                10,
//                0.157);

        // Project A
        new EconomicsCalculator().calculate(1_600,
                7_500_000,
                35_000_000,
                15_000_000,
                60,
                0.98,
                0.10,
                6,
                6,
                1_000_000,
                15,
                0.35);
    }

    public static void main(String[] args) {
        launch();
    }
}