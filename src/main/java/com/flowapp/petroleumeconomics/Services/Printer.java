package com.flowapp.petroleumeconomics.Services;

import com.flowapp.petroleumeconomics.Utils.TableList;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Printer {

    private final boolean printOut;
    private StringBuilder steps;

    Printer(boolean printOut) {
        this.printOut = printOut;
        this.steps = new StringBuilder();
    }

    public void renderTable(List<Object[]> args) {
        renderTable(args.toArray(new Object[0][0]));
    }

    public void renderTable(Object[] ... args) {
        final var temp = args[0];
        final String[] firstRow = new String[temp.length];
        for (int i = 0; i < temp.length; i++) {
            firstRow[i] = temp[i].toString();
        }
        TableList at = new TableList(firstRow).withUnicode(true);
        final var newRows = Arrays.stream(args).skip(1).map(row -> {
            final String[] newRow = new String[row.length];
            for (int i = 0; i < row.length; i++) {
                final Object object = row[i];
                if (object instanceof Number) {
                    newRow[i] = formatNumber((Number) object);
                } else {
                    newRow[i] = object.toString();
                }
            }
            return newRow;
        }).collect(Collectors.toList());
        for (var row: newRows) {
            at.addRow(row);
        }
        String rend = at.render();
        println(rend);
    }

    public void println(@NotNull String pattern, Object... args) {
        final String message = format(pattern, args);
        steps.append(message).append('\n');
        if (printOut) {
            System.out.println(message);
        }
    }

    public void clear() {
        steps = new StringBuilder();
    }

    public String formatNumber(Number number) {
        final var value = number.floatValue();
        if (number instanceof Double) {
            return number.toString();
        } else if (value == 0) {
            return  "0";
        } if (value < 1 && value > -1) {
            return String.format("%.7f", value);
        } else {
            return String.format("%.4f", value).replace(".0000", "");
        }
    }

    @NotNull
    public String format(@NotNull String pattern, Object... args) {
        Pattern rePattern = Pattern.compile("\\{([0-9+-]*)}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = rePattern.matcher(pattern);
        int counter = -1;
        while (matcher.find()) {
            counter++;
            String number = matcher.group(1);
            if (number == null) {
                number = "";
            }
            if (!number.isBlank()) {
                if (number.equals("+")) {
                    number = "\\+";
                    counter++;
                } else if (number.equals("-")) {
                    counter--;
                } else {
                    counter = Integer.parseInt(number);
                }
            }
            counter = clamp(counter, 0, args.length - 1);
            String toChange = "\\{" + number + "}";
            Object object = args[counter];
            String objectString;
            if (object instanceof Number) {
                objectString = formatNumber((Number) object);
            } else {
                objectString = object.toString();
            }
            String result = objectString;
            pattern = pattern.replaceFirst(toChange, result);
        }
        return pattern;
    }

    private <T extends Comparable<T>> T clamp(T val, T min, T max) {
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }
}