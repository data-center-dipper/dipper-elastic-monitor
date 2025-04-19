package com.dipper.monitor.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.*;

public class ListUtils {

    /**
     * 按照固定大小切分列表。
     *
     * @param <T>       列表元素类型
     * @param list      要切分的原始列表
     * @param pageSize  每个子列表的大小
     * @return 包含多个子列表的列表
     */
    public static <T> List<List<T>> splitListBySize(List<T> list, int pageSize) {
        if (list == null || list.isEmpty() || pageSize <= 0) {
            return Collections.emptyList();
        }

        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += pageSize) {
            int end = Math.min(i + pageSize, list.size());
            result.add(new ArrayList<>(list.subList(i, end)));
        }
        return result;
    }

    /**
     * 按次数切分列表。
     *
     * @param <T>       列表元素类型
     * @param list      要切分的原始列表
     * @param splitCount 总共要分成多少份
     * @return 包含多个子列表的列表
     */
    public static <T> List<List<T>> splitListByCount(List<T> list, int splitCount) {
        if (list == null || list.isEmpty() || splitCount <= 0) {
            return Collections.emptyList();
        }

        List<List<T>> result = new ArrayList<>();
        int size = list.size();
        int pageSize = (int) Math.ceil((double) size / splitCount);

        for (int i = 0; i < size; i += pageSize) {
            int end = Math.min(i + pageSize, size);
            result.add(new ArrayList<>(list.subList(i, end)));

            // 如果已经达到了所需的分割次数，则提前退出循环
            if (result.size() >= splitCount) {
                break;
            }
        }

        // 如果剩余元素不足一个完整的分页，将它们添加到最后一个分页中
        if (result.size() < splitCount && !list.isEmpty()) {
            int lastIdx = result.size() - 1;
            if (lastIdx >= 0) {
                List<T> lastPage = result.get(lastIdx);
                List<T> remainingElements = list.subList(lastPage.size() + lastIdx * pageSize, size);
                lastPage.addAll(remainingElements);
            }
        }

        return result;
    }

}
