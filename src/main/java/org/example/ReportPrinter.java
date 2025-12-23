package org.example;

import java.util.*;

public class ReportPrinter {

    public static void print(PreferenceProfile profile, CriteriaCalculator.Criteria c,
                             WeightAnalyzer.Result r, double step) {

        int m = profile.candidates.size();

        System.out.println("Общая информация");
        System.out.printf("Число альтернатив : %d%n", m);
        System.out.printf("Число избирателей : %d%n", profile.getTotalVoters());
        System.out.printf("Шаг : %.3f%n", step);
        System.out.println();

        System.out.println("Параметры альтернатив");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.printf("%-30s %8s %6s %6s%n", "Кандидат", "fB", "s", "d");
        System.out.println("-----------------------------------------------------------------------------");

        for (int i = 0; i < m; i++) {
            System.out.printf(Locale.ROOT,
                    "%-30s %8.1f %6d %6d%n",
                    profile.candidates.get(i),
                    c.fB[i], c.s[i], c.d[i]);
        }

        System.out.println("-----------------------------------------------------------------------------");
        System.out.println();

        System.out.println("Устойчивость побед");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.printf("%-30s %22s %22s%n",
                "Кандидат", "Единоличная победа", "Победа с ничьей");
        System.out.println("-----------------------------------------------------------------------------");

        for (int i = 0; i < m; i++) {
            System.out.printf(Locale.ROOT,
                    "%-30s %19.2f %% %19.2f %% %n",
                    profile.candidates.get(i),
                    100 * r.strictWins[i] / r.combinations,
                    100 * r.anyWins[i] / r.combinations);
        }

        System.out.println("-----------------------------------------------------------------------------");
        System.out.printf(Locale.ROOT,
                "Комбинаций с ничьёй : %.0f (%.2f %%)%n",
                r.tieCount,
                100 * r.tieCount / r.combinations);


    }
}
