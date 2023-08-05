package com.fly.paperhub.common.utils;

public final class StringUtil {

    public static String connect(String connector, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; ++i) {
            sb.append(parts[i]);
            if (i < parts.length - 1) sb.append(connector);
        }
        return sb.toString();
    }

}
