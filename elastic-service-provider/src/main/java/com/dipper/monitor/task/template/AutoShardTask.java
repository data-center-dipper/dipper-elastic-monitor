package com.dipper.monitor.task.template;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.shard.history.ShardHistoryItem;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.task.AbstractITask;
import com.dipper.monitor.task.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class AutoShardTask  extends AbstractITask  {

    private static final Logger log = LoggerFactory.getLogger(FeatureIndexCreateTask.class);

    @Autowired
    protected ElasticStoreTemplateService elasticStoreTemplateService;



    public void authShardTask() throws Exception {
        List<EsTemplateEntity> allTemplates = elasticStoreTemplateService.getAllTemplates();
        if (allTemplates == null || allTemplates.isEmpty()) return;

        // 过滤出已启用且开启自动分片的模板
        List<EsTemplateEntity> autoShardsTemplates = allTemplates.stream()
                .filter(template -> Boolean.TRUE.equals(template.getEnable()))
                .filter(template -> Boolean.TRUE.equals(template.getAutoShards()))
                .collect(Collectors.toList());

        for (EsTemplateEntity item : autoShardsTemplates) {
            List<ShardHistoryItem> historyItems = elasticStoreTemplateService.getTemplateShardHistory(item.getId());
            if (historyItems == null || historyItems.size() < 2) continue;

            Integer targetShardSizeGB = item.getShardSize();
            if (targetShardSizeGB == null || targetShardSizeGB <= 0) {
                log.warn("模板 {} 的目标分片大小未设置或无效", item.getEnName());
                continue;
            }

            // 只取最近 5 条记录用于趋势分析
            int limit = Math.min(10, historyItems.size());
            List<ShardHistoryItem> recentHistory = historyItems.subList(historyItems.size() - limit, historyItems.size());

            // 每个分片的平均大小
            List<Double> avgShardSizes = recentHistory.stream()
                    .map(h -> h.getShardSize())
                    .collect(Collectors.toList());

            // 趋势分析：使用线性回归法判断趋势方向
            double slope = calculateSlope(avgShardSizes);

            log.info("模板 {} 当前趋势系数: {}", item.getEnName(), slope);

            int currentShards = item.getNumberOfShards();

            int newShards = currentShards;

            // 判断趋势
            if (slope > 0.5) { // 明显上升趋势
                newShards = (int) Math.ceil((double) currentShards * 1.5);
                log.info("检测到分片大小持续上升，将分片数从 {} 提升至 {}", currentShards, newShards);
            } else if (slope < -0.5) { // 明显下降趋势
                newShards = Math.max(1, (int) Math.floor((double) currentShards / 1.5));
                log.info("检测到分片大小持续下降，将分片数从 {} 降低至 {}", currentShards, newShards);
            } else {
                log.info("模板 {} 分片大小趋势平稳，无需调整", item.getEnName());
                continue;
            }

            // 更新分片数
            try {
                elasticStoreTemplateService.updateTemplateShardNum(item.getId(), newShards);
                log.info("模板 {} 分片数从 {} 调整为 {}", item.getEnName(), currentShards, newShards);
            } catch (Exception e) {
                log.error("更新模板 {} 的分片数失败", item.getEnName(), e);
            }
        }
    }

        /**
         * 使用线性回归计算趋势斜率
         *
         * @param values 数值列表（如分片大小随时间变化）
         * @return 斜率（正表示上升趋势，负表示下降趋势）
         */
        private double calculateSlope(List<Double> values) {
            int n = values.size();
            double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;

            for (int i = 0; i < n; i++) {
                double x = i; // 时间点（假设等间隔）
                double y = values.get(i);

                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumXX += x * x;
            }

            double numerator = n * sumXY - sumX * sumY;
            double denominator = n * sumXX - sumX * sumX;

            if (denominator == 0) return 0;
            return numerator / denominator;
        }

    @Override
    public String getCron() {
        return "0 0/30 * * * ?";
    }

    @Override
    public void setCron(String cron) {

    }

    @Override
    public String getAuthor() {
        return "lcc";
    }

    @Override
    public String getJobDesc() {
        return "elastic 自动计算分片";
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void execute() {
        try {
            authShardTask();
        } catch (Exception e) {
            log.error("自动计算分片任务执行失败", e);
        }
    }

    @Override
    public String getTaskName() {
        return "authShardTask";
    }
}