package com.dipper.monitor.utils.elastic;

import org.apache.commons.lang3.StringUtils;

public class IndexUtils {
    public static boolean isIndexNameContainSpecialChar(String index) {
        if (StringUtils.isBlank(index)) {
            return true;
        }
        if (index.contains("{") || index.contains("}") || index
                .contains("+") || index
                .contains("|") || index.contains("||") || index
                .contains("!") || index
                .contains("(") || index.contains(")") || index
                .contains("[") || index.contains("]") || index
                .contains("?")) {
            return true;
        }
        return false;
    }
}