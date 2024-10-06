package github.kwizii.util;

public class StringUtil {

    public static boolean isBlank(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String delimitCamel(String camelCase, String delimiter) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        StringBuilder result = new StringBuilder();
        for (char ch : camelCase.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                if (result.length() > 0) {
                    result.append(delimiter);
                }
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
