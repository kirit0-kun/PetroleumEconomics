package com.flowapp.petroleumeconomics.Services;

import com.flowapp.petroleumeconomics.Utils.EconomicsUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class EconomicsCalculator {

    final double internalRateOfReturnPrecision = 0.00001;
    final Printer printer = new Printer(true);

    public void calculate(
            final double initialProductionPerWellPerDay,
            final double wellInitialCost,
            final double facilitiesCost,
            final double pipelinesCost,
            final double oilPricePerBarrel,
            final double upTimeFraction,
            final double interestRatePerYear,
            final int numberOfWells,
            final double operatingCostPerBarrel,
            final double abandonmentCost,
            final double abandonmentOilRatePerWellPerDay,
            final double declineRatePerYear) {
        printer.clear();

        final var wellsCost = numberOfWells * wellInitialCost;
        final var initialInvestment = facilitiesCost + pipelinesCost + wellsCost;

        final List<Float> cashFlows = new ArrayList<>();
        cashFlows.add((float) -initialInvestment);

        double totalOilProduction = 0;
        double lastProductionPerWellPerDay = initialProductionPerWellPerDay;
        while (lastProductionPerWellPerDay >= abandonmentOilRatePerWellPerDay) {
            final var oilProduction = upTimeFraction * 365 * lastProductionPerWellPerDay * numberOfWells;
            final var cashIn = oilProduction * oilPricePerBarrel;
            final var operatingCost = oilProduction * operatingCostPerBarrel;
            final var netCashFlow = cashIn - operatingCost;

            cashFlows.add((float) netCashFlow);

            totalOilProduction += oilProduction;
            lastProductionPerWellPerDay *= (1-declineRatePerYear);
        }

        final var years = cashFlows.size()- 1;

        cashFlows.set(years, (float) (cashFlows.get(years)-abandonmentCost));

        final float npv = (float) calculateNPV(cashFlows, interestRatePerYear);
        final float irr = (float) calculateIRR(cashFlows);
        final float pir = (float) EconomicsUtils.calculateProfitabilityIndex(npv, initialInvestment);

        renderTableAnnually(cashFlows, interestRatePerYear);

        printer.println("NPV = {} MM USD", printer.formatNumber(npv / 1_000_000));
        printer.println("IRR = {} %", printer.formatNumber(irr * 100));
        printer.println("PIR = {}", printer.formatNumber(pir));
    }

    private void renderTableAnnually(@NotNull List<Float> cashFlows, double discountRate) {
        final List<Object> years = new ArrayList<>(List.of(""));
        final List<Object> cf = new ArrayList<>(List.of("Cash flow (USD)"));
        final List<Object> df = new ArrayList<>(List.of("Discount factor"));
        final List<Object> pv = new ArrayList<>(List.of("Present Value (USD)"));
        final List<Object> apv = new ArrayList<>(List.of("Accumulated PV"));
        final var accumulatedPV = calculateAPVs(cashFlows, discountRate);
        years.addAll(getYears(cashFlows));
        cf.addAll(cashFlows);
        df.addAll(getDiscountFactor(cashFlows, discountRate));
        pv.addAll(calculatePVs(cashFlows, discountRate));
        apv.addAll(accumulatedPV);
        drawLines(cashFlows, accumulatedPV);
        printer.renderTable(
                years.toArray(),
                cf.toArray(),
                df.toArray(),
                pv.toArray(),
                apv.toArray()
        );
    }

    private List<Integer> getYears(@NotNull List<Float> cashFlows) {
        final List<Integer> years = new ArrayList<>();
        for (int i = 0; i < cashFlows.size(); i++) {
            years.add(i);
        }
        return  years;
    }

    private List<Float> getDiscountFactor(@NotNull List<Float> cashFlows, double discountRate) {
        final List<Float> discountFactors = new ArrayList<>();
        for (int i = 0; i < cashFlows.size(); i++) {
            discountFactors.add((float) EconomicsUtils.calculateDiscountFactor(discountRate, i));
        }
        return  discountFactors;
    }

    private double calculateIRR(@NotNull List<Float> cashFlows) {
        double lastDiscountRate;
        double discountRate = 0;
        double lastNpv;
        double npv = 0;

        do {
            lastDiscountRate = discountRate;
            lastNpv = npv;
            discountRate += internalRateOfReturnPrecision;
            npv = calculateNPV(cashFlows, discountRate);
//            System.out.println("" + npv + " " + discountRate);
        } while (npv > 0);
        return lastDiscountRate + (discountRate - lastDiscountRate) * (lastNpv / (lastNpv + Math.abs(npv)));
    }

    private List<Float> calculatePVs(@NotNull List<Float> cashFlows, double discountRate) {
        final List<Float> npvs = new ArrayList<>();
        for (int i = 0; i < cashFlows.size(); i++) {
            final var cashFlow = cashFlows.get(i);
            final var npv = EconomicsUtils.calculatePresentValue(cashFlow, discountRate, i);
            npvs.add((float) npv);
        }
        return  npvs;
    }

    private List<Float> calculateAPVs(@NotNull List<Float> cashFlows, double discountRate) {
        final List<Float> npvs = new ArrayList<>();
        double npv = 0;
        for (int i = 0; i < cashFlows.size(); i++) {
            final var cashFlow = cashFlows.get(i);
            npv += EconomicsUtils.calculatePresentValue(cashFlow, discountRate, i);
            npvs.add((float) npv);
        }
        return  npvs;
    }

    private double calculateNPV(@NotNull List<Float> cashFlows, double discountRate) {
        return calculateNPV(calculatePVs(cashFlows, discountRate));
    }

    private double calculateNPV(@NotNull List<Float> discountedCashFlows) {
        //noinspection OptionalGetWithoutIsPresent
        return discountedCashFlows.stream().reduce(Float::sum).get();
    }

    private void drawLines(List<Float> cashFlow, List<Float> cumulativePV) {
        XYChart.Series<String, Number> cashFlowSeries = new XYChart.Series<>();
        cashFlowSeries.setName("Cash Flow");
        for (int i = 0; i < cashFlow.size(); i++) {
            final var point = cashFlow.get(i);
            cashFlowSeries.getData().add(new XYChart.Data<>(String.valueOf(i), point));
        }

        XYChart.Series<String, Number> cumulativePVSeries = new XYChart.Series<>();
        cumulativePVSeries.setName("NPV");
        for (int i = 0; i < cumulativePV.size(); i++) {
            final var point = cumulativePV.get(i);
            cumulativePVSeries.getData().add(new XYChart.Data<>(String.valueOf(i), point));
        }

        //Defining the x an y axes
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        //Setting labels for the axes
        xAxis.setLabel("Years");
        yAxis.setLabel("$");

        LineChart<String, Number> npv = new LineChart<>(xAxis, yAxis);
        BarChart<String, Number> cf = new BarChart<>(xAxis, yAxis);
        cf.setLegendVisible(false);
        cf.setAnimated(false);
        npv.getData().add(cumulativePVSeries);
        cf.getData().add(cashFlowSeries);

        npv.setLegendVisible(false);
        npv.setAnimated(false);
        npv.setCreateSymbols(true);
        npv.setAlternativeRowFillVisible(false);
        npv.setAlternativeColumnFillVisible(false);
        npv.setHorizontalGridLinesVisible(false);
        npv.setVerticalGridLinesVisible(false);
        npv.getXAxis().setVisible(false);
        npv.getYAxis().setVisible(false);
        npv.getStylesheets().addAll(Objects.requireNonNull(getClass().getResource("chart.css")).toExternalForm());

        for (var series: List.of(npv, cf)) {
            for (var item: series.getData()) {
                for (XYChart.Data<String, Number> entry : item.getData()) {
                    Tooltip t = new Tooltip("(" + entry.getXValue() + " , " + String.format("%.0f", entry.getYValue().doubleValue()) + ")");
                    t.setShowDelay(new Duration(50));
                    Tooltip.install(entry.getNode(), t);
                }
            }
        }

        //Creating a stack pane to hold the chart
        StackPane pane = new StackPane(cf, npv);
        pane.setPadding(new Insets(15, 15, 15, 15));
        pane.setStyle("-fx-background-color: BEIGE");
        //Setting the Scene
        Scene scene = new Scene(pane, 595, 350);
        final var chartsWindow = new Stage();
        chartsWindow.setTitle("Economics");
        chartsWindow.setScene(scene);
        chartsWindow.show();
    }
}
