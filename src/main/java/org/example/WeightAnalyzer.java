package org.example;

import java.util.*;

public class WeightAnalyzer {

    public static class Result {
        public final double[] strictWins;
        public final double[] anyWins;
        public final double tieCount;
        public final int combinations;

        public Result(double[] strictWins, double[] anyWins,
                      double tieCount, int combinations) {
            this.strictWins = strictWins;
            this.anyWins = anyWins;
            this.tieCount = tieCount;
            this.combinations = combinations;
        }
    }

    public static Result analyze(CriteriaCalculator.Criteria c,
                                 double step) {

        int m = c.fB.length;
        int steps = (int) Math.round(1.0 / step);

        double[] strictWins = new double[m];
        double[] anyWins = new double[m];
        double tieCount = 0;
        int combos = 0;

        for (int ia = 0; ia <= steps; ia++) {
            double a = ia * step;
            for (int ib = 0; ib <= steps - ia; ib++) {
                double b = ib * step;
                double g = 1.0 - a - b;
                if (g < 0) continue;

                combos++;
                double max = Double.NEGATIVE_INFINITY;
                double[] F = new double[m];

                for (int i = 0; i < m; i++) {
                    F[i] = a * c.fB[i] + b * c.s[i] - g * c.d[i];
                    max = Math.max(max, F[i]);
                }

                List<Integer> winners = new ArrayList<>();
                for (int i = 0; i < m; i++)
                    if (Math.abs(F[i] - max) < 1e-9) winners.add(i);

                if (winners.size() == 1) strictWins[winners.get(0)]++;
                else tieCount++;

                for (int w : winners) anyWins[w]++;
            }
        }

        return new Result(strictWins, anyWins, tieCount, combos);
    }
}
