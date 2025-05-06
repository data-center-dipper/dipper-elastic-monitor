package com.dipper.monitor.entity.db.config;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ConfItemEntity {

    private Integer id;
    private String moduleName;
    private String entityName;
    private String sectionName;

    private String configType;
    private boolean customType;
    private boolean showView;
    private boolean dynamicData;

    private String configKey;
    private String configName;
    private String configValue;
    private String configDesc = "";
    private String configContent;

    private String createTime;
    private String updateTime;

    /**
     * 校验配置项实体的合法性。
     * @return 如果校验失败，则返回错误信息列表；如果校验成功，则返回空列表。
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (configName == null || configName.trim().isEmpty()) {
            errors.add("configName cannot be blank");
        } else if (configName.length() > 256) {
            errors.add("configName must not exceed 256 characters");
        }

        if (configValue != null && configValue.length() > 256) {
            errors.add("configValue must not exceed 256 characters");
        }

        if (configContent != null && configContent.length() > 256) {
            errors.add("configContent must not exceed 256 characters");
        }

        if (configType == null) {
            errors.add("configType is required");
        } else if (!"String".equals(configType) && !"text".equals(configType) && !"int".equals(configType)
                && !"double".equals(configType) && !"boolean".equals(configType) && !"list".equals(configType)
                && !"JSON".equals(configType)) {
            // 使用字符串比较而不是正则表达式，以避免大小写敏感问题
            errors.add("Invalid configType. Allowed values are: String, text, int, double, boolean, list, JSON");
        }

        return errors;
    }
}