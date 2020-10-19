package com.senither.hypixel.statistics.weight;

import com.senither.hypixel.utils.NumberUtil;

public class Weight {

    private final double weight;
    private final double overflow;

    public Weight(double weight, double overflow) {
        this.weight = weight;
        this.overflow = overflow;
    }

    public Weight() {
        this(0D, 0D);
    }

    public double getWeight() {
        return weight;
    }

    public double getOverflow() {
        return overflow;
    }

    public Weight add(Weight weight) {
        return new Weight(
            weight.getWeight() + getWeight(),
            weight.getOverflow() + getOverflow()
        );
    }

    public String getTotalWeightStringified() {
        if (overflow == 0D) {
            return NumberUtil.formatNicelyWithDecimals(weight);
        }

        return String.format("%s (%s Total)",
            toString(),
            NumberUtil.formatNicelyWithDecimals(weight + overflow)
        );
    }

    @Override
    public String toString() {
        if (overflow == 0D) {
            return NumberUtil.formatNicelyWithDecimals(weight);
        }
        return NumberUtil.formatNicelyWithDecimals(weight)
            + " + " + NumberUtil.formatNicelyWithDecimals(overflow) + " Overflow";
    }
}
