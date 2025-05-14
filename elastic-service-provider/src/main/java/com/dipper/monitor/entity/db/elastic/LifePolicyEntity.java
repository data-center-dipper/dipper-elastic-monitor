package com.dipper.monitor.entity.db.elastic;

import lombok.Data;
import java.util.Date;

@Data
public class LifePolicyEntity {
    private Integer id;
    // 中文名称
    private String zhName;
    // 英文名称
    private String enName;
    /**
     * json格式的内容
     **/
    private String policyValue;
    // 创建时间
    private Date createTime;
    // 更新时间
    private Date updateTime;
}
