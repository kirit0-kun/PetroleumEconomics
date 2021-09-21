package com.flowapp.petroleumeconomics.Utils;

public class EconomicsUtils {

    ///
    /// Compound Interest
    ///
    static public double calculateInterestFactor(double interestRate, double timePeriod) {
        return Math.pow(1+ interestRate, timePeriod);
    }

    static public double calculateFutureValue(double currentValue, double interestRate, double timePeriod) {
        return calculateFutureValue(currentValue, calculateInterestFactor(interestRate, timePeriod));
    }

    static public double calculateFutureValue(double currentValue, double interestFactor) {
        return currentValue * interestFactor;
    }

    ///
    /// Present Value
    ///
    static public double calculateDiscountFactor(double discountRate, double timePeriod) {
        return Math.pow(1+discountRate, timePeriod);
    }

    static public double calculatePresentValue(double futureValue, double discountRate, double timePeriod) {
        return calculatePresentValue(futureValue, calculateDiscountFactor(discountRate, timePeriod));
    }

    static public double calculatePresentValue(double futureValue, double discountFactor) {
        return futureValue / discountFactor;
    }

    ///
    /// Profitability Index
    ///
    static public double calculateProfitabilityIndex(double netPresentValue, double discountedInvestment) {
        return netPresentValue / discountedInvestment;
    }
}
