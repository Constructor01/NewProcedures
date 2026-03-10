package org.example;

import java.util.*;


public class VotingProcedures {

    static String[] CANDIDATES;

    static class BallotType {
        int[] ranking;
        int count;
        BallotType(int[] ranking, int count) {
            this.ranking = ranking;
            this.count = count;
        }
    }

    static List<BallotType> ballotsFromParsedProfile(JsonParser.ParsedProfile profile) {
        List<BallotType> ballots = new ArrayList<>();
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < profile.alternatives.size(); i++) {
            indexMap.put(profile.alternatives.get(i), i);
        }
        for (JsonParser.Ranking r : profile.rankings) {
            int[] ranking = new int[r.order.size()];
            for (int i = 0; i < r.order.size(); i++) {
                ranking[i] = indexMap.get(r.order.get(i));
            }
            ballots.add(new BallotType(ranking, r.voters));
        }
        return ballots;
    }

    public static void main(String[] args) throws Exception {

        JsonParser.ParsedProfile profile = JsonParser.parse("src/main/resources/t_5000_11.json");

        CANDIDATES = profile.alternatives.toArray(new String[0]);
        List<BallotType> ballots = ballotsFromParsedProfile(profile);

        int m = CANDIDATES.length;
        int totalVoters = ballots.stream().mapToInt(b -> b.count).sum();

        System.out.println("Кандидаты: " + Arrays.toString(CANDIDATES));
        System.out.println("Всего избирателей: " + totalVoters);
        System.out.println();


        double[] borda = bordaScores(ballots, m);
        System.out.println("1) Процедура Борда:");
        printScores(borda);


        int[][] g = pairwiseMatrix(ballots, m);
        System.out.println("\nМатрица парных сравнений g(i,j):");
        printMatrix(g);


        double[] modBorda = modifiedBordaFromPairwise(g);
        System.out.println("\n2) Модифицированная процедура Борда:");
        printScores(modBorda);


        int[] simpsonFs = simpsonMaximin(g);
        System.out.println("\n3) Процедура Симпсона");
        for (int i = 0; i < m; i++) System.out.printf("%s: %d%n", CANDIDATES[i], simpsonFs[i]);
        int winnerSimpson = argmax(simpsonFs);
        System.out.println("Победитель: " + CANDIDATES[winnerSimpson]);


        int[] dodgson = dodgsonScores(g, totalVoters);
        System.out.println("\n4) Процедура Доджсона");
        for (int i = 0; i < m; i++) System.out.printf("%s: %d%n", CANDIDATES[i], dodgson[i]);
        int winnerDodgson = argmin(dodgson);
        System.out.println("Победитель: " + CANDIDATES[winnerDodgson]);


        System.out.println("\n5) Процедуры Нансона и Кумбса:");
        List<String> nansonOrder = nansonProcedure(ballots, m);
        System.out.println("Нансон (от лучшего к худшему) = " + nansonOrder);

        List<String> kumbsOrder = kumbsProcedure(g);
        System.out.println("Кумбс (от лучшего к худшему) = " + kumbsOrder);


        int[] copeland = copelandScores(g);
        System.out.println("\n6) Процедура Коупленда");
        for (int i = 0; i < m; i++) System.out.printf("%s: %d%n", CANDIDATES[i], copeland[i]);
        int winnerCopeland = argmax(copeland);
        System.out.println("Победитель: " + CANDIDATES[winnerCopeland]);


        int[] fishburn = fishburnScores(g);
        System.out.println("\n7) Процедура Фишберна");
        for (int i = 0; i < m; i++) System.out.printf("%s: %d%n", CANDIDATES[i], fishburn[i]);
        int winnerFishburn = argmax(fishburn);
        System.out.println("Победитель: " + CANDIDATES[winnerFishburn]);
    }

    static void printScores(double[] scores) {
        for (int i = 0; i < scores.length; i++) {
            System.out.printf("%s: %.4f%n", CANDIDATES[i], scores[i]);
        }
        int w = argmax(scores);
        System.out.println("Победитель: " + CANDIDATES[w]);
    }

    static void printMatrix(int[][] mtx) {
        int n = mtx.length;
        System.out.print("    ");
        for (int j = 0; j < n; j++) System.out.printf("%4s", CANDIDATES[j]);
        System.out.println();
        for (int i = 0; i < n; i++) {
            System.out.printf("%3s", CANDIDATES[i]);
            for (int j = 0; j < n; j++) System.out.printf("%4d", mtx[i][j]);
            System.out.println();
        }
    }

    static int argmax(int[] arr) {
        int best = 0;
        for (int i = 1; i < arr.length; i++) if (arr[i] > arr[best]) best = i;
        return best;
    }

    static int argmin(int[] arr) {
        int best = 0;
        for (int i = 1; i < arr.length; i++) if (arr[i] < arr[best]) best = i;
        return best;
    }

    static int argmax(double[] arr) {
        int best = 0;
        for (int i = 1; i < arr.length; i++) if (arr[i] > arr[best]) best = i;
        return best;
    }

    static double[] bordaScores(List<BallotType> ballots, int m) {
        double[] scores = new double[m];
        for (BallotType b : ballots) {
            for (int pos = 0; pos < m; pos++) {
                int candidate = b.ranking[pos];
                scores[candidate] += (m - 1 - pos) * b.count;
            }
        }
        return scores;
    }

    static int[][] pairwiseMatrix(List<BallotType> ballots, int m) {
        int[][] g = new int[m][m];
        for (BallotType b : ballots) {
            int[] posOf = new int[m];
            for (int p = 0; p < m; p++) {
                posOf[b.ranking[p]] = p;
            }
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    if (i != j && posOf[i] < posOf[j]) {
                        g[i][j] += b.count;
                    }
                }
            }
        }
        return g;
    }

    static double[] modifiedBordaFromPairwise(int[][] g) {
        int m = g.length;
        double[] fBM = new double[m];
        for (int i = 0; i < m; i++) {
            double s = 0;
            for (int j = 0; j < m; j++) if (i != j) s += (g[i][j] - g[j][i]);
            fBM[i] = s;
        }
        return fBM;
    }

    static int[] simpsonMaximin(int[][] g) {
        int m = g.length;
        int[] fs = new int[m];
        for (int i = 0; i < m; i++) {
            int mn = Integer.MAX_VALUE;
            for (int j = 0; j < m; j++) if (i != j) mn = Math.min(mn, g[i][j]);
            fs[i] = mn;
        }
        return fs;
    }

    static int[] dodgsonScores(int[][] g, int totalVoters) {
        int m = g.length;
        int maj;
        if(totalVoters%2==0){
            maj = totalVoters / 2+1;
        }else{
            maj = (totalVoters+1) / 2;
        }

        int[] fD = new int[m];
        for (int i = 0; i < m; i++) {
            int sum = 0;
            for (int j = 0; j < m; j++){
                if (i != j){
                    sum += Math.max(0, maj - g[i][j]);
                }
            }
            fD[i] = sum;
        }
        return fD;
    }

    static List<String> nansonProcedure(List<BallotType> ballots, int mInitial) {
        Set<Integer> active = new LinkedHashSet<>();
        for (int i = 0; i < mInitial; i++) active.add(i);
        List<String> eliminationOrder = new ArrayList<>();
        while (active.size() > 1) {
            double[] scores = new double[mInitial];
            for (BallotType b : ballots) {
                List<Integer> orderActive = new ArrayList<>();
                for (int cand : b.ranking) if (active.contains(cand)) orderActive.add(cand);
                for (int pos = 0; pos < orderActive.size(); pos++)
                    scores[orderActive.get(pos)] += (orderActive.size() - 1 - pos) * b.count;
            }
            double mn = Double.POSITIVE_INFINITY;
            int minc = -1;
            for (int c : active) if (scores[c] < mn) { mn = scores[c]; minc = c; }
            active.remove(minc);
            eliminationOrder.add(CANDIDATES[minc]);
        }
        List<String> result = new ArrayList<>();
        for (int c : active) result.add(CANDIDATES[c]);
        Collections.reverse(eliminationOrder);
        result.addAll(eliminationOrder);
        return result;
    }

    static List<String> kumbsProcedure(int[][] g) {
        int m = g.length;
        Set<Integer> active = new LinkedHashSet<>();
        for (int i = 0; i < m; i++) active.add(i);
        List<String> eliminated = new ArrayList<>();
        while (active.size() > 1) {
            int worst = -1, worstLosses = -1;
            for (int i : new ArrayList<>(active)) {
                int losses = 0;
                for (int j : active) if (i != j && g[j][i] > g[i][j]) losses++;
                if (losses > worstLosses) { worstLosses = losses; worst = i; }
            }
            active.remove(worst);
            eliminated.add(CANDIDATES[worst]);
        }
        List<String> result = new ArrayList<>();
        for (int i : active) result.add(CANDIDATES[i]);
        Collections.reverse(eliminated);
        result.addAll(eliminated);
        return result;
    }

    static int[] copelandScores(int[][] g) {
        int m = g.length;
        int[] fc = new int[m];
        for (int i = 0; i < m; i++) {
            int wins = 0, losses = 0;
            for (int j = 0; j < m; j++) if (i != j) {
                if (g[i][j] > g[j][i]) wins++; else if (g[i][j] < g[j][i]) losses++;
            }
            fc[i] = wins - losses;
        }
        return fc;
    }

    static int[] fishburnScores(int[][] g) {
        int m = g.length;
        int[] f = new int[m];
        for (int i = 0; i < m; i++) {
            int wins = 0;
            for (int j = 0; j < m; j++) if (i != j && g[i][j] > g[j][i]) wins++;
            f[i] = wins;
        }
        return f;
    }
}
