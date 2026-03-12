package org.example;

import java.util.*;

import java.nio.file.Path;
import java.nio.file.Paths;



public class Application {

    public static void main(String[] args) throws Exception {

        double step = 0.05;
        String jsonFile = "src/main/resources/t_5000_25.json";

        Path output = Paths.get("src/main/resources/report.json");

        JsonParser.ParsedProfile parsed = JsonParser.parse(jsonFile);

        long startTime = System.nanoTime();//начало времени

        Map<List<String>, Integer> rankings = new LinkedHashMap<>();
        for (JsonParser.Ranking r : parsed.rankings) {
            rankings.merge(r.order, r.voters, Integer::sum);
        }

        PreferenceProfile profile = new PreferenceProfile(parsed.alternatives, rankings);

        int[][] G = PairwiseMatrixBuilder.build(profile);

        CriteriaCalculator.Criteria criteria = CriteriaCalculator.compute(G, profile.getQuorum());

        WeightAnalyzer.Result result = WeightAnalyzer.analyze(criteria, step);

        long endTime = System.nanoTime();//конец времени
        long difTime = endTime - startTime;
        long resultTime = difTime / 1_000_000;//в мс
        System.out.println("Время выполнения алгоритма: "+resultTime+" (мс)\n");

        ReportPrinter.print(profile, criteria, result, step);

        ReportPrinter.writeJsonReport(
                profile,
                criteria,
                result,
                step,
                output
        );
    }
}
