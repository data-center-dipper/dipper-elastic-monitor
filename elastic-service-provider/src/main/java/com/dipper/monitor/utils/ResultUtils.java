package com.dipper.monitor.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.enums.MonitorResultEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于构建API响应结果的工具类。该工具类提供了一系列静态方法来创建JSON格式的成功、失败等响应。
 */
public class ResultUtils {

    /**
     * JSON响应中的状态码键名。
     */
    public static final String CODE = "code";

    /**
     * JSON响应中的消息键名。
     */
    public static final String MESSAGE = "message";

    /**
     * JSON响应中的数据键名。
     */
    public static final String DATA = "data";



    /**
     * 构建一个成功响应，包含指定的数据。
     *
     * @param data 响应中携带的数据对象
     * @return 包含成功信息和数据的JSONObject
     */
    public static JSONObject onSuccess(Object data) {
        return new JSONObject()
                .fluentPut(CODE, 0)
                .fluentPut(MESSAGE, "操作成功")
                .fluentPut(DATA, data);
    }

    /**
     * 构建一个仅表示成功的响应，不携带额外数据。
     *
     * @return 包含成功信息的JSONObject
     */
    public static JSONObject onSuccess() {
        return new JSONObject()
                .fluentPut(CODE, 0)
                .fluentPut(MESSAGE, "操作成功");
    }

    /**
     * 构建一个带有自定义成功评论的成功响应。
     *
     * @param comment 自定义的成功评论信息
     * @return 包含自定义成功信息的JSONObject
     */
    public static JSONObject onSuccessWithComment(String comment) {
        return new JSONObject()
                .fluentPut(CODE, 0)
                .fluentPut(MESSAGE, comment);
    }

    /**
     * 构建一个分页成功的响应，包含总条目数和数据列表。
     *
     * @param total 总条目数
     * @param data  数据列表
     * @return 包含分页信息和数据的JSONObject
     */
    public static JSONObject onSuccessWithPageTotal(long total, Object data) {
        return new JSONObject()
                .fluentPut(CODE, 0)
                .fluentPut(MESSAGE, "操作成功")
                .fluentPut("total", total)
                .fluentPut(DATA, data);
    }

    /**
     * 构建一个失败响应，包含指定的错误信息。
     *
     * @param error 错误信息描述
     * @return 包含错误信息的JSONObject
     */
    public static JSONObject onFail(String error) {
        return new JSONObject()
                .fluentPut(CODE, MonitorResultEnum.UNKNOWN_ERROR.code)
                .fluentPut(MESSAGE, error);
    }

    public static JSONObject onFail(int code, String message) {
        return new JSONObject()
                .fluentPut(CODE, code)
                .fluentPut(MESSAGE, message);
    }

    /**
     * 构建一个仅表示失败的响应，使用默认的未知错误信息。
     *
     * @return 包含未知错误信息的JSONObject
     */
    public static JSONObject onFail() {
        return new JSONObject()
                .fluentPut(CODE, MonitorResultEnum.UNKNOWN_ERROR.code)
                .fluentPut(MESSAGE, "未知错误，请稍后重试或联系管理员");
    }

    /**
     * 构建一个基于枚举定义的失败响应。
     *
     * @param monitorResultEnum 定义了错误码和消息的枚举项
     * @return 包含枚举定义的错误信息的JSONObject
     */
    public static JSONObject onFail(MonitorResultEnum monitorResultEnum) {
        return new JSONObject()
                .fluentPut(CODE, monitorResultEnum.code)
                .fluentPut(MESSAGE, monitorResultEnum.name());
    }

    /**
     * 构建一个带有自定义错误信息和枚举定义的失败响应。
     *
     * @param error      自定义的错误信息描述
     * @param monitorResultEnum 定义了错误码和消息的枚举项
     * @return 包含自定义错误信息和枚举定义的JSONObject
     */
    public static JSONObject onFail(String error, MonitorResultEnum monitorResultEnum) {
        return new JSONObject()
                .fluentPut(CODE, monitorResultEnum.code)
                .fluentPut(MESSAGE, error);
    }

    /**
     * 构建一个警告响应，包含指定的警告信息。
     *
     * @param warning 警告信息描述
     * @return 包含警告信息的JSONObject
     */
    public static JSONObject onWarning(String warning) {
        return new JSONObject()
                .fluentPut(CODE, MonitorResultEnum.WARNING_ERROR.code)
                .fluentPut(MESSAGE, warning);
    }

    /**
     * 构建一个部分失败但有成功项目的响应。
     *
     * @param successItems 成功处理的项目列表
     * @param error        错误信息描述
     * @param monitorResultEnum   定义了错误码和消息的枚举项
     * @return 包含部分失败信息和成功项目的JSONObject
     */
    public static JSONObject onHalfFailWithSuccessItem(String successItems, String error, MonitorResultEnum monitorResultEnum) {
        return new JSONObject()
                .fluentPut(CODE, monitorResultEnum.code)
                .fluentPut(MESSAGE, error)
                .fluentPut("successItems", successItems);
    }

    /**
     * 构建一个带有成功项目的成功响应。
     *
     * @param successItems 成功处理的项目列表
     * @return 包含成功信息和成功项目的JSONObject
     */
    public static JSONObject onSuccessWithSuccessItem(String successItems) {
        return new JSONObject()
                .fluentPut(CODE, MonitorResultEnum.SUCCESS.code)
                .fluentPut(MESSAGE, "操作成功")
                .fluentPut("successItems", successItems);
    }

    /**
     * 构建一个带格式化错误信息的失败响应。
     *
     * @param sentence   格式化的错误信息模板
     * @param monitorResultEnum 定义了错误码和消息的枚举项
     * @param item       用于格式化错误信息的参数
     * @return 包含格式化错误信息的JSONObject
     */
    public static JSONObject onFail(String sentence, MonitorResultEnum monitorResultEnum, Object... item) {
        return new JSONObject()
                .fluentPut(CODE, monitorResultEnum.code)
                .fluentPut(MESSAGE, String.format(sentence, item));
    }

    /**
     * 构建一个基于枚举定义的失败响应，同时支持格式化错误信息。
     *
     * @param monitorResultEnum 定义了错误码和消息的枚举项
     * @param item       用于格式化错误信息的参数
     * @return 包含枚举定义和格式化错误信息的JSONObject
     */
    public static JSONObject onFail(MonitorResultEnum monitorResultEnum, Object... item) {
        return new JSONObject()
                .fluentPut(CODE, monitorResultEnum.code)
                .fluentPut(MESSAGE, String.format(monitorResultEnum.name(), item));
    }

    /**
     * 构建一个批量处理部分失败的响应。
     *
     * @param monitorResultEnum 定义了错误码和消息的枚举项
     * @param data       响应中携带的数据对象
     * @return 包含部分失败信息和数据的JSONObject
     */
    public static JSONObject onHalfFailForBatch(MonitorResultEnum monitorResultEnum, Object data) {
        return new JSONObject()
                .fluentPut(CODE, monitorResultEnum.code)
                .fluentPut(MESSAGE, monitorResultEnum.name())
                .fluentPut(DATA, data);
    }

    /**
     * 从API响应中提取特定字段的数据，并转换为目标类型。
     *
     * @param result API响应结果
     * @param field  需要提取的字段名称
     * @param clazz  目标类型
     * @param <T>    泛型类型
     * @return 提取并转换后的数据对象
     * @throws RuntimeException 如果响应的状态码不是0，则抛出异常
     */
    public static <T> T getResultData(Object result, String field, Class<T> clazz) {
        JSONObject resultJson = JSON.parseObject(JSON.toJSONString(result));
        if (resultJson.getInteger(CODE).intValue() != 0) {
            throw new RuntimeException(resultJson.toJSONString());
        }
        return resultJson.getObject(field, clazz);
    }

    /**
     * 从API响应中提取"data"字段的数据，并转换为目标类型。
     *
     * @param result API响应结果
     * @param clazz  目标类型
     * @param <T>    泛型类型
     * @return 提取并转换后的数据对象
     * @throws RuntimeException 如果响应的状态码不是0，则抛出异常
     */
    public static <T> T getResultData(Object result, Class<T> clazz) {
        return getResultData(result, DATA, clazz);
    }

    /**
     * 从API响应中提取"data"字段的数据列表，并转换为目标类型列表。
     *
     * @param result API响应结果
     * @param clazz  目标类型的Class对象
     * @param <T>    泛型类型
     * @return 提取并转换后的数据列表
     * @throws RuntimeException 如果响应的状态码不是0，则抛出异常
     */
    public static <T> List<T> getResultDataList(Object result, Class<T> clazz) {
        JSONObject resultJson = (JSONObject) result;
        if (resultJson.getInteger(CODE).intValue() != 0) {
            throw new RuntimeException(resultJson.toJSONString());
        }
        List objects = resultJson.getObject(DATA, List.class);
        List<T> results = new ArrayList<>();
        for (Object object : objects) {
            results.add(JSON.parseObject(JSON.toJSONString(object), clazz));
        }
        return results;
    }

    /**
     * 处理分页逻辑，返回分页后的数据和总数。
     *
     * @param c     数据集合
     * @param page  当前页码
     * @param size  每页大小
     * @return 分页后的数据和总数的JSONObject
     */
    public static JSONObject pageHandle(List<?> c, Integer page, Integer size) {
        while ((page - 1) * size > c.size()) {
            page--;
        }
        int start = (page - 1) * size;
        int end = Math.min(page * size, c.size());
        return onSuccess(c.subList(start, end)).fluentPut("total", c.size());
    }


}