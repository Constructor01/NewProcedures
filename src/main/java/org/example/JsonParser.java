package org.example;

import java.nio.file.*;
import java.util.*;

public class JsonParser {

    public static class Ranking {
        public List<String> order;
        public int voters;

        public Ranking(List<String> order, int voters) {
            this.order = order;
            this.voters = voters;
        }
    }

    public static class ParsedProfile {
        public List<String> alternatives = new ArrayList<>();
        public List<Ranking> rankings = new ArrayList<>();
    }

    public static ParsedProfile parse(String filePath) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");


        ParsedProfile result = new ParsedProfile();

        // alternatives
        String alternativesBlock = extractArray(json, "\"alternatives\"");
        result.alternatives = extractStringArray(alternativesBlock);

        // rankings
        String rankingsBlock = extractArray(json, "\"rankings\"");
        List<String> objects = splitObjects(rankingsBlock);

        for (String obj : objects) {
            String orderBlock = extractArray(obj, "\"order\"");
            List<String> order = extractStringArray(orderBlock);
            int voters = extractInt(obj, "\"voters\"");
            result.rankings.add(new Ranking(order, voters));
        }

        return result;
    }



    private static String extractArray(String text, String key) {
        int k = text.indexOf(key);
        int start = text.indexOf('[', k);
        int depth = 1;
        int i = start + 1;
        while (depth > 0) {
            if (text.charAt(i) == '[') depth++;
            else if (text.charAt(i) == ']') depth--;
            i++;
        }
        return text.substring(start + 1, i - 1);
    }

    private static List<String> extractStringArray(String text) {
        List<String> res = new ArrayList<>();
        boolean in = false;
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '"') {
                if (in) {
                    res.add(sb.toString());
                    sb.setLength(0);
                }
                in = !in;
            } else if (in) {
                sb.append(c);
            }
        }
        return res;
    }

    private static List<String> splitObjects(String text) {
        List<String> res = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    res.add(text.substring(start, i + 1));
                }
            }
        }
        return res;
    }

    private static int extractInt(String text, String key) {
        int k = text.indexOf(key);
        int c = text.indexOf(':', k);
        int i = c + 1;
        while (!Character.isDigit(text.charAt(i))) i++;
        int s = i;
        while (Character.isDigit(text.charAt(i))) i++;
        return Integer.parseInt(text.substring(s, i));
    }
}
