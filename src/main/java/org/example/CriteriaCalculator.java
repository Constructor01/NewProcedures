package org.example;

public class CriteriaCalculator {

    public static class Criteria {
        public final double[] fB;
        public final int[] s;
        public final int[] d;

        public Criteria(double[] fB, int[] s, int[] d) {
            this.fB = fB;
            this.s = s;
            this.d = d;
        }
    }

    public static Criteria compute(int[][] G, int quorum) {

        int m = G.length;
        double[] fB = new double[m];
        int[] s = new int[m];
        int[] d = new int[m];

        for (int i = 0; i < m; i++) {
            int sum = 0;
            int min = Integer.MAX_VALUE;
            int deficit = 0;

            for (int j = 0; j < m; j++) if (j != i) {
                sum += G[i][j];
                min = Math.min(min, G[i][j]);
                if (G[i][j] < quorum) deficit += (quorum - G[i][j]);
            }

            fB[i] = sum;
            s[i] = (min == Integer.MAX_VALUE ? 0 : min);
            d[i] = deficit;
        }

        return new Criteria(fB, s, d);
    }
}
