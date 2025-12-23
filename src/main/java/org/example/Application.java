package org.example;

import java.util.*;

public class Application {

    public static void main(String[] args) throws Exception {

        double step = 0.05;
        String jsonFile = "src/main/resources/euros.json";

        JsonParser.ParsedProfile parsed =
                JsonParser.parse(jsonFile);

        Map<List<String>, Integer> rankings = new LinkedHashMap<>();
        for (JsonParser.Ranking r : parsed.rankings) {
            rankings.merge(r.order, r.voters, Integer::sum);
        }

        PreferenceProfile profile = new PreferenceProfile(parsed.alternatives, rankings);

        int[][] G = PairwiseMatrixBuilder.build(profile);

        CriteriaCalculator.Criteria criteria = CriteriaCalculator.compute(G, profile.getQuorum());

        WeightAnalyzer.Result result = WeightAnalyzer.analyze(criteria, step);

        ReportPrinter.print(profile, criteria, result, step);
    }
}
