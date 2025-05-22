package com.dipper.monitor.utils.elastic;

import com.dipper.monitor.entity.elastic.index.IndexEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class FeatureIndexUtils {



    /**
     * 判断索引是否是未来索引（尚未到来的时间段）
     *
     * indexName 可能是这样的 aabbcc
     * aabbcc-2023.01.01
     * aabbcc-20230101-000001
     * aabbcc-202301-000001
     * aabbcc-2023-000001
     * aabbcc-000001
     * aabbcc
     *
     * 我们只考虑索引是否带有时间，带有时间就取值横岗切分的倒数第二个，然后如果是年，那么是明年的就是未来索引
     * 如果是月，这个月和下个月的就是未来索引
     * 如果是日，今天和明天的就是未来索引
     *
     * @param indexName 索引名称
     * @return 是否是未来索引
     */
    public static boolean isFeatureIndex(String indexName) {
        String[] parts = indexName.split("-");
        if (parts.length < 2) {
            return false;
        }

        // 取倒数第二个部分作为时间字段
        String dateTimePart = parts[parts.length - 2];

        // 尝试匹配不同时间格式
        LocalDate indexDate = null;

        try {
            // 格式 1: yyyy.MM.dd （例如：2025.06.01）
            if (dateTimePart.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                indexDate = LocalDate.parse(dateTimePart, formatter);
            }
            // 格式 2: yyyyMMdd （例如：20250601）
            else if (dateTimePart.matches("\\d{8}")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                indexDate = LocalDate.parse(dateTimePart, formatter);
            }
            // 格式 3: yyyyMM （例如：202506）
            else if (dateTimePart.matches("\\d{6}")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
                indexDate = LocalDate.parse(dateTimePart + "01", formatter);
            }
            // 格式 4: yyyy （例如：2026）
            else if (dateTimePart.matches("\\d{4}")) {
                int year = Integer.parseInt(dateTimePart);
                int currentYear = LocalDate.now().getYear();
                return year > currentYear;
            }
        } catch (DateTimeParseException e) {
            return false; // 解析失败，不是合法时间格式
        }

        if (indexDate != null) {
            LocalDate now = LocalDate.now();
            return indexDate.isAfter(now); // 判断是否是今天之后的日期
        }

        return false;
    }

    public static List<IndexEntity> getFeatureIndex(List<IndexEntity> allIndexes, Boolean featureIndex) {
        if(!featureIndex){
            return allIndexes;
        }
        List<IndexEntity> filteredList = new ArrayList<>();
        for (IndexEntity item: allIndexes) {
            String index = item.getIndex();
            if(FeatureIndexUtils .isFeatureIndex(index)){
                filteredList.add(item);
            }
        }
        return filteredList;
    }
}