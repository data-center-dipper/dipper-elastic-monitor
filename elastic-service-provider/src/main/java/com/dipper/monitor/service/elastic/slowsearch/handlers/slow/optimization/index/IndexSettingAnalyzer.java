package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.slowsearch.slow.index.IndexSettingOptimization;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class IndexSettingAnalyzer {

    public List<IndexSettingOptimization> analyze(JSONObject setting) {
        List<IndexSettingOptimization> optimizations = new ArrayList<>();

        if (setting != null && !setting.isEmpty()) {
            JSONObject indexSetting = setting.getJSONObject("index");
            if (indexSetting != null) {
                checkReplicaCount(indexSetting, optimizations);
                checkRefreshInterval(indexSetting, optimizations);
                checkMergePolicySettings(indexSetting, optimizations);
                checkNumberOfShards(indexSetting, optimizations);
            }
        }

        return optimizations;
    }

    private void checkReplicaCount(JSONObject indexSetting, List<IndexSettingOptimization> optimizations) {
        if (indexSetting.containsKey("number_of_replicas")) {
            int replicas = Integer.parseInt(indexSetting.getString("number_of_replicas"));
            if (replicas == 0) {
                IndexSettingOptimization opt = new IndexSettingOptimization();
                opt.setSettingName("number_of_replicas");
                opt.setCurrentValue(String.valueOf(replicas));
                opt.setRecommendedValue("1");
                opt.setDescription("当前索引没有副本，存在数据丢失风险。建议至少配置1个副本。");
                opt.setImpact("high");
                optimizations.add(opt);
            } else if (replicas > 3) {
                IndexSettingOptimization opt = new IndexSettingOptimization();
                opt.setSettingName("number_of_replicas");
                opt.setCurrentValue(String.valueOf(replicas));
                opt.setRecommendedValue("2");
                opt.setDescription("当前索引副本数量过多，会增加写入负担和存储开销。建议根据节点数量适当减少副本数。");
                opt.setImpact("medium");
                optimizations.add(opt);
            }
        }
    }

    private void checkRefreshInterval(JSONObject indexSetting, List<IndexSettingOptimization> optimizations) {
        if (indexSetting.containsKey("refresh_interval")) {
            String refreshInterval = indexSetting.getString("refresh_interval");
            if (refreshInterval.equals("1s")) {
                IndexSettingOptimization opt = new IndexSettingOptimization();
                opt.setSettingName("refresh_interval");
                opt.setCurrentValue("1s");
                opt.setRecommendedValue("30s");
                opt.setDescription("当前索引刷新间隔较短，频繁刷新会影响查询性能。建议增加刷新间隔。");
                opt.setImpact("medium");
                optimizations.add(opt);
            }
        }
    }

    private void checkMergePolicySettings(JSONObject indexSetting, List<IndexSettingOptimization> optimizations) {
        if (indexSetting.containsKey("merge")) {
            JSONObject merge = indexSetting.getJSONObject("merge");
            if (merge != null && merge.containsKey("policy")) {
                JSONObject policy = merge.getJSONObject("policy");
                if (policy != null && policy.containsKey("max_merged_segment")) {
                    String maxMergedSegment = policy.getString("max_merged_segment");
                    if (maxMergedSegment.equals("5gb") || maxMergedSegment.equals("5mb")) {
                        IndexSettingOptimization opt = new IndexSettingOptimization();
                        opt.setSettingName("merge.policy.max_merged_segment");
                        opt.setCurrentValue(maxMergedSegment);
                        opt.setRecommendedValue("2gb");
                        opt.setDescription("合并策略的最大段大小设置不合理，可能导致合并效率低下。建议设置为适中值。");
                        opt.setImpact("medium");
                        optimizations.add(opt);
                    }
                }
            }
        }
    }

    private void checkNumberOfShards(JSONObject indexSetting, List<IndexSettingOptimization> optimizations) {
        if (indexSetting.containsKey("number_of_shards")) {
            int shards = Integer.parseInt(indexSetting.getString("number_of_shards"));
            if (shards > 20) {
                IndexSettingOptimization opt = new IndexSettingOptimization();
                opt.setSettingName("number_of_shards");
                opt.setCurrentValue(String.valueOf(shards));
                opt.setRecommendedValue("5");
                opt.setDescription("当前索引分片数量过多，建议根据数据量和节点数量调整分片数，避免过度分片。");
                opt.setImpact("high");
                optimizations.add(opt);
            } else if (shards == 1 && indexSetting.containsKey("store") && 
                    indexSetting.getJSONObject("store").containsKey("total_size_in_bytes") && 
                    Long.parseLong(indexSetting.getJSONObject("store").getString("total_size_in_bytes")) > 50 * 1024 * 1024 * 1024L) {
                // 如果索引大小超过50GB但只有1个分片
                IndexSettingOptimization opt = new IndexSettingOptimization();
                opt.setSettingName("number_of_shards");
                opt.setCurrentValue("1");
                opt.setRecommendedValue("5");
                opt.setDescription("当前索引数据量较大但只有1个分片，可能导致单分片过大，影响性能。建议增加分片数量。");
                opt.setImpact("high");
                optimizations.add(opt);
            }
        }
    }
}