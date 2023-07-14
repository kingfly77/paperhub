package com.fly.paperhub.common.utils;

import java.util.List;

public class ListUtil {

    public static String toString(List list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Object obj: list) {
            sb.append(obj.toString()).append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

}
