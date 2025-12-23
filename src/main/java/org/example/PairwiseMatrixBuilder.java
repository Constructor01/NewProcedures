package org.example;

import java.util.*;

public class PairwiseMatrixBuilder {

    public static int[][] build(PreferenceProfile profile) {

        int m = profile.candidates.size();
        int[][] G = new int[m][m];

        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < m; i++) {
            index.put(profile.candidates.get(i), i);
        }

        for (Map.Entry<List<String>, Integer> e : profile.rankings.entrySet()) {
            List<String> ranking = e.getKey();
            int voters = e.getValue();

            for (int i = 0; i < ranking.size(); i++) {
                for (int j = i + 1; j < ranking.size(); j++) {
                    int hi = index.get(ranking.get(i));
                    int lo = index.get(ranking.get(j));
                    G[hi][lo] += voters;
                }
            }
        }
        return G;
    }
}
