package org.example;

public class KendallCorrelation {

    public static double tau(int[] r1, int[] r2) {

        int n = r1.length;

        int C = 0;
        int D = 0;

        for (int i = 0; i < n; i++) {

            for (int j = i + 1; j < n; j++) {

                int a = Integer.compare(r1[i], r1[j]);
                int b = Integer.compare(r2[i], r2[j]);

                if (a == b)
                    C++;
                else
                    D++;
            }
        }

        double denom = n * (n - 1) / 2.0;

        return (C - D) / denom;
    }

    public static double similarityPercent(int[] r1, int[] r2) {

        double tau = tau(r1, r2);

        return (tau + 1) / 2 * 100.0;
    }
}