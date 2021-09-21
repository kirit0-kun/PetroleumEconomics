package com.flowapp.petroleumeconomics;

import com.flowapp.petroleumeconomics.Services.EconomicsCalculator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Runner {
    public static void main(String[] args) {
        new EconomicsCalculator().calculate();
    }
}