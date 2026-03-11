package org.example;

import java.util.Arrays;
import java.util.Locale;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class ReportPrinter {

    public static void print(PreferenceProfile profile,
                             CriteriaCalculator.Criteria c,
                             WeightAnalyzer.Result r,
                             double step) {

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
                    c.fB[i],
                    c.s[i],
                    c.d[i]);
        }

        System.out.println("-----------------------------------------------------------------------------");
        System.out.println();

        printBlock("Полная процедура", profile, r.full);
        printBlock("Комбинация Борда + Симпсон (B + S)", profile, r.bsOnly);
        printBlock("Комбинация Борда + Доджсон (B + D)", profile, r.bdOnly);
        printBlock("Процедура Борда", profile, r.borda);
        printBlock("Процедура Симпсона", profile, r.simpson);
        printBlock("Процедура Доджсона", profile, r.dodgson);

        printKendall(profile, r);
    }

    private static void printBlock(String title,
                                   PreferenceProfile profile,
                                   WeightAnalyzer.Stats stats) {

        int m = profile.candidates.size();

        System.out.println("==============================================================================");
        System.out.printf("%53s",title);
        System.out.printf("\n");
        //System.out.println(title);
        System.out.println("-----------------------------------------------------------------------------");
        System.out.printf("%-30s %22s %22s%n",
                "Кандидат", "Единоличная победа", "Победа с ничьей");
        System.out.println("-----------------------------------------------------------------------------");

        for (int i = 0; i < m; i++) {

            System.out.printf(Locale.ROOT,
                    "%-30s %19.2f %% %19.2f %% %n",
                    profile.candidates.get(i),
                    100.0 * stats.strictWins[i] / stats.combinations,
                    100.0 * stats.anyWins[i] / stats.combinations);
        }

        System.out.println("-----------------------------------------------------------------------------");
        System.out.printf(Locale.ROOT,
                "Комбинаций с ничьёй : %.0f (%.2f %%)%n",
                stats.tieCount,
                100.0 * stats.tieCount / stats.combinations);

        System.out.println();

        printRanking(profile, stats);
    }

    private static void printRanking(PreferenceProfile profile,
                                     WeightAnalyzer.Stats stats) {

        int m = profile.candidates.size();

        Integer[] order = new Integer[m];

        for (int i = 0; i < m; i++)
            order[i] = i;

        Arrays.sort(order,
                (a,b) -> Double.compare(stats.avgRank[a], stats.avgRank[b]));

        System.out.println("Итоговая средняя ранжировка альтернатив");
        System.out.println("-----------------------------------------------------------------------------");

        System.out.printf("%-30s %12s %12s %12s%n",
                "Кандидат",
                "avg rank",
                "best",
                "worst");

        System.out.println("-----------------------------------------------------------------------------");

        for (int i = 0; i < m; i++) {

            int idx = order[i];

            System.out.printf(Locale.ROOT,
                    "%-30s %12.3f %12d %12d%n",
                    profile.candidates.get(idx),
                    stats.avgRank[idx],
                    stats.bestRank[idx],
                    stats.worstRank[idx]);
        }

        System.out.println("-----------------------------------------------------------------------------");
        System.out.println();
    }

    public static void writeJsonReport(PreferenceProfile profile,
                                       CriteriaCalculator.Criteria c,
                                       WeightAnalyzer.Result r,
                                       double step,
                                       Path outputFile) throws IOException {

        int m = profile.candidates.size();

        StringBuilder json = new StringBuilder();

        json.append("{\n");

        json.append("  \"summary\": {\n");
        json.append("    \"alternatives\": ").append(m).append(",\n");
        json.append("    \"voters\": ").append(profile.getTotalVoters()).append(",\n");
        json.append("    \"step\": ").append(String.format(Locale.ROOT, "%.3f", step)).append("\n");
        json.append("  },\n");


        json.append("  \"alternatives\": [\n");

        for (int i = 0; i < m; i++) {

            json.append("    {\n");
            json.append("      \"name\": \"").append(profile.candidates.get(i)).append("\",\n");
            json.append("      \"fB\": ").append(c.fB[i]).append(",\n");
            json.append("      \"s\": ").append(c.s[i]).append(",\n");
            json.append("      \"d\": ").append(c.d[i]).append(",\n");

            json.append("      \"full_avg_rank\": ").append(r.full.avgRank[i]).append(",\n");
            json.append("      \"full_best_rank\": ").append(r.full.bestRank[i]).append(",\n");
            json.append("      \"full_worst_rank\": ").append(r.full.worstRank[i]).append(",\n");

            json.append("      \"full_strict_win_percent\": ")
                    .append(String.format(Locale.ROOT,"%.2f",
                            100.0 * r.full.strictWins[i] / r.full.combinations))
                    .append(",\n");

            json.append("      \"full_any_win_percent\": ")
                    .append(String.format(Locale.ROOT,"%.2f",
                            100.0 * r.full.anyWins[i] / r.full.combinations))
                    .append("\n");

            json.append("    }");

            if (i < m - 1) json.append(",");

            json.append("\n");
        }

        json.append("  ],\n");


        json.append("  \"procedures\": {\n");

        json.append("    \"full\": {\n");
        json.append("      \"ties\": ").append(r.full.tieCount).append(",\n");
        json.append("      \"combinations\": ").append(r.full.combinations).append("\n");
        json.append("    },\n");

        json.append("    \"borda_simpson\": {\n");
        json.append("      \"ties\": ").append(r.bsOnly.tieCount).append(",\n");
        json.append("      \"combinations\": ").append(r.bsOnly.combinations).append("\n");
        json.append("    },\n");

        json.append("    \"borda_dodgson\": {\n");
        json.append("      \"ties\": ").append(r.bdOnly.tieCount).append(",\n");
        json.append("      \"combinations\": ").append(r.bdOnly.combinations).append("\n");
        json.append("    }\n");

        json.append("  }\n");

        json.append("}\n");

        Files.write(outputFile, json.toString().getBytes("UTF-8"));
    }

    private static void printKendall(PreferenceProfile profile,
                                     WeightAnalyzer.Result r) {

        int[] full = WeightAnalyzer.rankingFromStats(r.full);
        int[] bs = WeightAnalyzer.rankingFromStats(r.bsOnly);
        int[] bd = WeightAnalyzer.rankingFromStats(r.bdOnly);
        int[] borda = WeightAnalyzer.rankingFromStats(r.borda);
        int[] simpson = WeightAnalyzer.rankingFromStats(r.simpson);
        int[] dodgson = WeightAnalyzer.rankingFromStats(r.dodgson);

        System.out.println("Сходство ранжировок (Kendall similarity %)");
        System.out.println("-----------------------------------------------------");

        printSim("Full vs B+S", full, bs);
        printSim("Full vs B+D", full, bd);
        printSim("Full vs Borda", full, borda);
        printSim("Full vs Simpson", full, simpson);
        printSim("Full vs Dodgson", full, dodgson);

        System.out.println("-----------------------------------------------------");
    }
    private static void printSim(String name, int[] r1, int[] r2) {

        double sim = KendallCorrelation.similarityPercent(r1, r2);

        System.out.printf(java.util.Locale.ROOT,
                "%-25s : %6.2f %%\n",
                name,
                sim);
    }
}