package com.flowapp.petroleumeconomics.Services;

import com.flowapp.petroleumeconomics.Utils.EconomicsUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class EconomicsCalculator {

    final double internalRateOfReturnPrecision = 0.001;
    final Printer printer = new Printer(true);

    public void calculate() {
        printer.clear();
//        final double initialProductionPerWellPerDay = 1_600;
//        final double wellInitialCost = 7_500_000;
//        final double facilitiesCost = 35_000_000;
//        final double pipelinesCost = 15_000_000;
//        final double oilPricePerBarrel = 60;
//        final double upTimeFraction = 0.98;
//        final double interestRatePerYear = 0.10;
//        final int numberOfWells = 6;
//        final double operatingCostPerBarrel = 6;
//        final double abandonmentCost = 1_000_000;
//        final double abandonmentOilRatePerWellPerDay = 15;
//        final double declineRatePerYear = 0.35;

        final double initialProductionPerWellPerDay = 500;
        final double wellInitialCost = 5_000_000;
        final double facilitiesCost = 25_000_000;
        final double pipelinesCost = 20_000_000;
        final double oilPricePerBarrel = 60;
        final double upTimeFraction = 0.98;
        final double interestRatePerYear = 0.07;
        final int numberOfWells = 10;
        final double operatingCostPerBarrel = 4;
        final double abandonmentCost = 1_500_000;
        final double abandonmentOilRatePerWellPerDay = 10;
        final double declineRatePerYear = 0.157;

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
        years.addAll(getYears(cashFlows));
        cf.addAll(cashFlows);
        df.addAll(getDiscountFactor(cashFlows, discountRate));
        pv.addAll(calculatePVs(cashFlows, discountRate));
        apv.addAll(calculateAPVs(cashFlows, discountRate));
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
            System.out.println("" + npv + " " + discountRate);
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
}
