package com.dipper.monitor.service.elastic.template.impl.handlers.history;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.TemplatePageInfo;
import com.dipper.monitor.entity.elastic.template.history.EsTemplateInfo;
import com.dipper.monitor.entity.elastic.template.history.TemplateDetailView;
import com.dipper.monitor.entity.elastic.template.history.TemplateHistoryView;
import com.dipper.monitor.utils.ListUtils;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 除去系统模版的都是业务模版，包括当前时间的模版
 */
@Slf4j
@Component
public class BusinessHistoryTemplateHandler extends AbstractHistoryTemplateHandler {

    /**
     * 获取业务模板（不以.开头的模板）
     *
     * @param templatePageInfo 分页信息
     * @return 业务模板列表和总数
     */
    public Tuple2<Integer, List<TemplateHistoryView>> getBusenessUseTemplate(TemplatePageInfo templatePageInfo) {
        try {
            // 获取所有模板并转换为TemplateHistoryView
            List<TemplateHistoryView> allTemplates = getAllTemplates();

            // 过滤出业务模板（不以.开头的模板）
            List<TemplateHistoryView> businessTemplates = allTemplates.stream()
                    .filter(template -> template.getName() == null || !template.getName().startsWith("."))
                    .collect(Collectors.toList());

            // 如果有关键词搜索，进一步过滤
            if (templatePageInfo != null && StringUtils.hasText(templatePageInfo.getKeyword())) {
                String keyword = templatePageInfo.getKeyword().toLowerCase();
                businessTemplates = businessTemplates.stream()
                        .filter(template ->
                                (template.getName() != null && template.getName().toLowerCase().contains(keyword)) ||
                                        (template.getIndexPatterns() != null && template.getIndexPatterns().toLowerCase().contains(keyword))
                        )
                        .collect(Collectors.toList());
            }

            int total = businessTemplates.size();

            if (total == 0) {
                return new Tuple2<>(0, new ArrayList<>());
            }

            // 分页处理
            if (templatePageInfo != null && templatePageInfo.getPageNum() != null && templatePageInfo.getPageSize() != null) {
                int pageNum = Math.max(1, templatePageInfo.getPageNum());
                int pageSize = Math.max(1, templatePageInfo.getPageSize());

                List<List<TemplateHistoryView>> pagedLists = ListUtils.splitListBySize(businessTemplates, pageSize);

                if (pageNum > pagedLists.size()) {
                    return new Tuple2<>(total, new ArrayList<>());
                }

                businessTemplates = pagedLists.get(pageNum - 1);
            } else {
                // 默认分页参数
                int pageNum = 1;
                int pageSize = 10;

                if (total > pageSize) {
                    List<List<TemplateHistoryView>> pagedLists = ListUtils.splitListBySize(businessTemplates, pageSize);
                    businessTemplates = pagedLists.get(0);
                }
            }

            return new Tuple2<>(total, businessTemplates);
        } catch (Exception e) {
            log.error("获取业务模板分页信息失败", e);
            return new Tuple2<>(0, new ArrayList<>());
        }
    }



}