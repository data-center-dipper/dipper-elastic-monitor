package com.dipper.monitor.utils.elastic;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static List<String> getAliansListFromAliansSet(JSONObject aliansJson) {
        List<String> aliansList = new ArrayList<>();
        JSONObject jsonObject = (JSONObject) JSONPath.eval(aliansJson, "$.aliases");
        for (Map.Entry<String, Object> item : (Iterable<Map.Entry<String, Object>>)jsonObject.entrySet()) {
            String aliansName = item.getKey();
            aliansList.add(aliansName);
        }
        return aliansList;
    }
}