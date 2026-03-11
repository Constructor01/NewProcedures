package org.example;

import java.util.*;

public class WeightAnalyzer {

    public static class Stats {

        public final double[] strictWins;
        public final double[] anyWins;

        public final double tieCount;
        public final int combinations;

        public final double[] avgRank;
        public final int[] bestRank;
        public final int[] worstRank;

        public Stats(double[] strictWins,
                     double[] anyWins,
                     double tieCount,
                     int combinations,
                     double[] avgRank,
                     int[] bestRank,
                     int[] worstRank) {

            this.strictWins = strictWins;
            this.anyWins = anyWins;
            this.tieCount = tieCount;
            this.combinations = combinations;

            this.avgRank = avgRank;
            this.bestRank = bestRank;
            this.worstRank = worstRank;
        }
    }

    public static class Result {

        public final Stats full;
        public final Stats bsOnly;
        public final Stats bdOnly;

        public final Stats borda;
        public final Stats simpson;
        public final Stats dodgson;

        public Result(Stats full,
                      Stats bsOnly,
                      Stats bdOnly,
                      Stats borda,
                      Stats simpson,
                      Stats dodgson) {

            this.full = full;
            this.bsOnly = bsOnly;
            this.bdOnly = bdOnly;

            this.borda = borda;
            this.simpson = simpson;
            this.dodgson = dodgson;
        }
    }

    public static Result analyze(CriteriaCalculator.Criteria c, double step) {

        Stats full = analyzeFull(c, step);
        Stats bsOnly = analyzeBS(c, step);
        Stats bdOnly = analyzeBD(c, step);

        Stats borda = analyzeBorda(c);
        Stats simpson = analyzeSimpson(c);
        Stats dodgson = analyzeDodgson(c);

        return new Result(full, bsOnly, bdOnly, borda, simpson, dodgson);
    }

    private static Stats analyzeBorda(CriteriaCalculator.Criteria c) {
        return analyzeMaxCriterion(c.fB);
    }

    private static Stats analyzeSimpson(CriteriaCalculator.Criteria c) {

        double[] values = new double[c.s.length];

        for (int i = 0; i < values.length; i++)
            values[i] = c.s[i];

        return analyzeMaxCriterion(values);
    }

    private static Stats analyzeDodgson(CriteriaCalculator.Criteria c) {

        double[] values = new double[c.d.length];

        for (int i = 0; i < values.length; i++)
            values[i] = -c.d[i];

        return analyzeMaxCriterion(values);
    }

    private static Stats analyzeMaxCriterion(double[] values) {

        int m = values.length;

        double[] strictWins = new double[m];
        double[] anyWins = new double[m];

        double[] rankSum = new double[m];
        int[] bestRank = new int[m];
        int[] worstRank = new int[m];

        Arrays.fill(bestRank, Integer.MAX_VALUE);
        Arrays.fill(worstRank, Integer.MIN_VALUE);

        double max = Double.NEGATIVE_INFINITY;

        for (double v : values)
            max = Math.max(max, v);

        List<Integer> winners = new ArrayList<>();

        for (int i = 0; i < m; i++)
            if (Math.abs(values[i] - max) < 1e-9)
                winners.add(i);

        if (winners.size() == 1)
            strictWins[winners.get(0)] = 1;

        for (int w : winners)
            anyWins[w] = 1;

        Integer[] order = new Integer[m];

        for (int i = 0; i < m; i++)
            order[i] = i;

        Arrays.sort(order,(a,b)->Double.compare(values[b],values[a]));

        for (int pos = 0; pos < m; pos++) {

            int alt = order[pos];
            int rank = pos + 1;

            rankSum[alt] = rank;

            bestRank[alt] = rank;
            worstRank[alt] = rank;
        }

        double[] avgRank = new double[m];

        for (int i = 0; i < m; i++)
            avgRank[i] = rankSum[i];

        return new Stats(strictWins, anyWins, 0, 1, avgRank, bestRank, worstRank);
    }


    private static Stats analyzeFull(CriteriaCalculator.Criteria c, double step) {

        int m = c.fB.length;
        int steps = (int) Math.round(1.0 / step);

        double[] strictWins = new double[m];
        double[] anyWins = new double[m];

        double[] rankSum = new double[m];
        int[] bestRank = new int[m];
        int[] worstRank = new int[m];

        Arrays.fill(bestRank, Integer.MAX_VALUE);
        Arrays.fill(worstRank, Integer.MIN_VALUE);

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

                List<Integer> winners = getWinners(F, max);

                if (winners.size() == 1) strictWins[winners.get(0)]++;
                else tieCount++;

                for (int w : winners) anyWins[w]++;

                Integer[] order = new Integer[m];
                for (int i = 0; i < m; i++) order[i] = i;

                Arrays.sort(order, (x,y) -> Double.compare(F[y], F[x]));

                for (int pos = 0; pos < m; pos++) {

                    int alt = order[pos];
                    int rank = pos + 1;

                    rankSum[alt] += rank;

                    bestRank[alt] = Math.min(bestRank[alt], rank);
                    worstRank[alt] = Math.max(worstRank[alt], rank);
                }
            }
        }

        double[] avgRank = new double[m];

        for (int i = 0; i < m; i++)
            avgRank[i] = rankSum[i] / combos;

        return new Stats(strictWins, anyWins, tieCount, combos, avgRank, bestRank, worstRank);
    }

    private static Stats analyzeBS(CriteriaCalculator.Criteria c, double step) {

        int m = c.fB.length;
        int steps = (int) Math.round(1.0 / step);

        double[] strictWins = new double[m];
        double[] anyWins = new double[m];

        double[] rankSum = new double[m];
        int[] bestRank = new int[m];
        int[] worstRank = new int[m];

        Arrays.fill(bestRank, Integer.MAX_VALUE);
        Arrays.fill(worstRank, Integer.MIN_VALUE);

        double tieCount = 0;
        int combos = 0;

        for (int ia = 0; ia <= steps; ia++) {

            double a = ia * step;
            double b = 1.0 - a;

            if (b < -1e-12) continue;

            combos++;

            double max = Double.NEGATIVE_INFINITY;
            double[] F = new double[m];

            for (int i = 0; i < m; i++) {
                F[i] = a * c.fB[i] + b * c.s[i];
                max = Math.max(max, F[i]);
            }

            List<Integer> winners = getWinners(F, max);

            if (winners.size() == 1) strictWins[winners.get(0)]++;
            else tieCount++;

            for (int w : winners) anyWins[w]++;

            Integer[] order = new Integer[m];
            for (int i = 0; i < m; i++) order[i] = i;

            Arrays.sort(order, (x,y) -> Double.compare(F[y], F[x]));

            for (int pos = 0; pos < m; pos++) {

                int alt = order[pos];
                int rank = pos + 1;

                rankSum[alt] += rank;

                bestRank[alt] = Math.min(bestRank[alt], rank);
                worstRank[alt] = Math.max(worstRank[alt], rank);
            }
        }

        double[] avgRank = new double[m];

        for (int i = 0; i < m; i++)
            avgRank[i] = rankSum[i] / combos;

        return new Stats(strictWins, anyWins, tieCount, combos, avgRank, bestRank, worstRank);
    }

    private static Stats analyzeBD(CriteriaCalculator.Criteria c, double step) {

        int m = c.fB.length;
        int steps = (int) Math.round(1.0 / step);

        double[] strictWins = new double[m];
        double[] anyWins = new double[m];

        double[] rankSum = new double[m];
        int[] bestRank = new int[m];
        int[] worstRank = new int[m];

        Arrays.fill(bestRank, Integer.MAX_VALUE);
        Arrays.fill(worstRank, Integer.MIN_VALUE);

        double tieCount = 0;
        int combos = 0;

        for (int ia = 0; ia <= steps; ia++) {

            double a = ia * step;
            double g = 1.0 - a;

            if (g < -1e-12) continue;

            combos++;

            double max = Double.NEGATIVE_INFINITY;
            double[] F = new double[m];

            for (int i = 0; i < m; i++) {
                F[i] = a * c.fB[i] - g * c.d[i];
                max = Math.max(max, F[i]);
            }

            List<Integer> winners = getWinners(F, max);

            if (winners.size() == 1) strictWins[winners.get(0)]++;
            else tieCount++;

            for (int w : winners) anyWins[w]++;

            Integer[] order = new Integer[m];
            for (int i = 0; i < m; i++) order[i] = i;

            Arrays.sort(order, (x,y) -> Double.compare(F[y], F[x]));

            for (int pos = 0; pos < m; pos++) {

                int alt = order[pos];
                int rank = pos + 1;

                rankSum[alt] += rank;

                bestRank[alt] = Math.min(bestRank[alt], rank);
                worstRank[alt] = Math.max(worstRank[alt], rank);
            }
        }

        double[] avgRank = new double[m];

        for (int i = 0; i < m; i++)
            avgRank[i] = rankSum[i] / combos;

        return new Stats(strictWins, anyWins, tieCount, combos, avgRank, bestRank, worstRank);
    }

    private static List<Integer> getWinners(double[] F, double max) {

        List<Integer> winners = new ArrayList<>();

        for (int i = 0; i < F.length; i++) {
            if (Math.abs(F[i] - max) < 1e-9)
                winners.add(i);
        }

        return winners;
    }

    public static int[] rankingFromStats(Stats stats) {

        int m = stats.avgRank.length;

        Integer[] order = new Integer[m];

        for (int i = 0; i < m; i++)
            order[i] = i;

        java.util.Arrays.sort(order,
                (a,b)->Double.compare(stats.avgRank[a], stats.avgRank[b]));

        int[] rank = new int[m];

        for (int i = 0; i < m; i++)
            rank[order[i]] = i + 1;

        return rank;
    }
}