package org.example;

import java.util.*;

public class PreferenceProfile {

    public final List<String> candidates;
    public final Map<List<String>, Integer> rankings;

    public PreferenceProfile(List<String> candidates,
                             Map<List<String>, Integer> rankings) {
        this.candidates = candidates;
        this.rankings = rankings;
    }

    public int getTotalVoters() {
        return rankings.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getQuorum() {
        int t = getTotalVoters();
        return (t % 2 == 0) ? (t / 2 + 1) : ((t + 1) / 2);
    }
}
