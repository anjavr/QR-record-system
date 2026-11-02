package com.evidencija.evidencija_backend.service;

import java.util.List;

public class GroupRulesService {

    public record IndexRange(String label, int start, int end, String category) {}

    public static final List<IndexRange> RANGES = List.of(
            new IndexRange("G1", 1101, 1159, "G"),
            new IndexRange("G2", 1160, 11117, "G"),
            new IndexRange("G3", 11118, 11123, "G"),
            new IndexRange("G3", 1201, 1252, "G"),
            new IndexRange("G4", 1253, 1255, "G"),
            new IndexRange("G4", 1301, 1353, "G"),

            new IndexRange("L1", 1101, 1121, "L"),
            new IndexRange("L2", 1122, 1144, "L"),
            new IndexRange("L3", 1145, 1165, "L"),
            new IndexRange("L4", 1166, 1188, "L"),
            new IndexRange("L5", 1189, 11108, "L"),
            new IndexRange("L6", 11109, 11123, "L"),
            new IndexRange("L6", 1201, 1206, "L"),
            new IndexRange("L7", 1207, 1226, "L"),
            new IndexRange("L8", 1227, 1249, "L"),
            new IndexRange("L9", 1251, 1255, "L"),
            new IndexRange("L9", 1301, 1316, "L"),
            new IndexRange("L10", 1317, 1337, "L"),
            new IndexRange("L11", 1338, 1353, "L")
    );

    public static final List<String> G_LABELS = List.of("G1", "G2", "G3", "G4");
    public static final List<String> L_LABELS = List.of("L1", "L2", "L3", "L4", "L5", "L6", "L7", "L8", "L9", "L10", "L11");

    public static String category(String activityType) {
        return "LAB".equals(activityType) ? "L" : "G";
    }

    public static List<String> labelsForCategory(String category) {
        return "L".equals(category) ? L_LABELS : G_LABELS;
    }

    public static String findGroup(int indexNumber, String category) {
        for (IndexRange r : RANGES) {
            if (r.category().equals(category) && indexNumber >= r.start() && indexNumber <= r.end()) {
                return r.label();
            }
        }
        return null;
    }

    public static int parseIndexNumber(String studentNumber) {
        if (studentNumber == null) return -1;
        String numericPart = studentNumber.split("/")[0].replaceAll("[^0-9]", "");
        if (numericPart.isEmpty()) return -1;
        try {
            return Integer.parseInt(numericPart);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}