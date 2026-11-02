package com.evidencija.evidencija_backend.service.util;

import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CyrillicTransliterator {

    private static final Map<Character, String> MAP = new LinkedHashMap<>();
    static {
        MAP.put('а', "a"); MAP.put('б', "b"); MAP.put('в', "v"); MAP.put('г', "g");
        MAP.put('д', "d"); MAP.put('ђ', "dj"); MAP.put('е', "e"); MAP.put('ж', "z");
        MAP.put('з', "z"); MAP.put('и', "i"); MAP.put('ј', "j"); MAP.put('к', "k");
        MAP.put('л', "l"); MAP.put('љ', "lj"); MAP.put('м', "m"); MAP.put('н', "n");
        MAP.put('њ', "nj"); MAP.put('о', "o"); MAP.put('п', "p"); MAP.put('р', "r");
        MAP.put('с', "s"); MAP.put('т', "t"); MAP.put('ћ', "c"); MAP.put('у', "u");
        MAP.put('ф', "f"); MAP.put('х', "h"); MAP.put('ц', "c"); MAP.put('ч', "c");
        MAP.put('џ', "dz"); MAP.put('ш', "s");
    }

    public String toLatinAscii(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : input.toLowerCase().toCharArray()) {
            sb.append(MAP.getOrDefault(c, String.valueOf(c)));
        }
        return sb.toString().replaceAll("[^a-z0-9]", "");
    }

    public String toLatinDisplay(String input) {
        if (input == null || input.isBlank()) return input;

        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            boolean isUpper = Character.isUpperCase(c);
            char lower = Character.toLowerCase(c);
            String replacement = MAP.getOrDefault(lower, String.valueOf(lower));

            if (isUpper && !replacement.isEmpty()) {
                sb.append(Character.toUpperCase(replacement.charAt(0)));
                sb.append(replacement.substring(1));
            } else {
                sb.append(replacement);
            }
        }
        return sb.toString();
    }
}