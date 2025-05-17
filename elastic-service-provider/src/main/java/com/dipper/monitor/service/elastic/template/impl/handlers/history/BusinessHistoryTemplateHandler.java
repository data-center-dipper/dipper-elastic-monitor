package com.dipper.monitor.service.elastic.template.impl.handlers.history;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.TemplatePageInfo;
import com.dipper.monitor.entity.elastic.template.history.EsTemplateInfo;
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
            
            // 获取总数
            int total = businessTemplates.size();
            
            // 如果没有数据，直接返回空列表
            if (total == 0) {
                return new Tuple2<>(0, new ArrayList<>());
            }

            // 分页处理
            if (templatePageInfo != null && templatePageInfo.getPageNum() != null && templatePageInfo.getPageSize() != null) {
                int pageNum = Math.max(1, templatePageInfo.getPageNum());
                int pageSize = Math.max(1, templatePageInfo.getPageSize());
                
                // 使用ListUtils.splitListBySize进行分页
                List<List<TemplateHistoryView>> pagedLists = ListUtils.splitListBySize(businessTemplates, pageSize);
                
                // 防止页码越界
                if (pageNum > pagedLists.size()) {
                    return new Tuple2<>(total, new ArrayList<>());
                }
                
                // 获取指定页的数据（注意索引从0开始，而页码从1开始）
                businessTemplates = pagedLists.get(pageNum - 1);
            } else {
                // 如果没有提供分页信息，使用默认分页参数
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
    
    /**
     * 获取所有模板并转换为TemplateHistoryView
     * @return 模板列表
     */
    private List<TemplateHistoryView> getAllTemplates() {
        try {
            // 获取模板基本信息
            List<EsTemplateInfo> templateInfoList = elasticRealTemplateService.getTemplateList();
            
            // 转换为TemplateHistoryView
            List<TemplateHistoryView> result = new ArrayList<>();
            for (EsTemplateInfo info : templateInfoList) {
                TemplateHistoryView view = new TemplateHistoryView();
                view.setName(info.getName());
                view.setOrder(info.getOrder());
                
                // 处理索引模式
                if (info.getIndexPatterns() != null && !info.getIndexPatterns().isEmpty()) {
                    view.setIndexPatterns(String.join(", ", info.getIndexPatterns()));
                }
                
                // 获取模板详情
                try {
                    JSONObject templateDetail = elasticRealTemplateService.getOneTemplateDetail(info.getName());
                    if (templateDetail != null) {
                        // 设置详情内容
                        view.setContent(templateDetail.toJSONString());
                        
                        // 尝试提取更多信息
                        extractTemplateDetails(view, templateDetail);
                    }
                } catch (Exception e) {
                    log.warn("获取模板{}详情失败: {}", info.getName(), e.getMessage());
                }
                
                result.add(view);
            }
            
            return result;
        } catch (IOException e) {
            log.error("获取模板列表失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 从模板详情中提取更多信息
     * @param view 模板视图对象
     * @param templateDetail 模板详情JSON
     */
    private void extractTemplateDetails(TemplateHistoryView view, JSONObject templateDetail) {
        try {
            // 尝试提取滚动策略、保存策略等信息
            // 这里的实现取决于templateDetail的具体结构
            // 以下是示例代码，需要根据实际JSON结构调整
            
            if (templateDetail.containsKey("index_templates")) {
                JSONObject indexTemplate = templateDetail.getJSONArray("index_templates").getJSONObject(0)
                    .getJSONObject("index_template");
                
                if (indexTemplate.containsKey("template") && 
                    indexTemplate.getJSONObject("template").containsKey("settings") &&
                    indexTemplate.getJSONObject("template").getJSONObject("settings").containsKey("index")) {
                    
                    JSONObject settings = indexTemplate.getJSONObject("template")
                        .getJSONObject("settings").getJSONObject("index");
                    
                    // 提取生命周期策略
                    if (settings.containsKey("lifecycle")) {
                        JSONObject lifecycle = settings.getJSONObject("lifecycle");
                        if (lifecycle.containsKey("name")) {
                            view.setRollingPolicy(lifecycle.getString("name"));
                        }
                    }
                    
                    // 提取其他可能的信息
                    if (settings.containsKey("number_of_shards")) {
                        view.setIndexNum(settings.getString("number_of_shards"));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取模板{}详细信息失败: {}", view.getName(), e.getMessage());
        }
    }
}
