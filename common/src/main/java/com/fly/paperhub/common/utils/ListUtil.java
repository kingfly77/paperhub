package com.fly.paperhub.common.utils;

import java.util.ArrayList;
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

    public static <T> List<T> castToList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }
}
