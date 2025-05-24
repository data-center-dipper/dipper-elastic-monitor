//package com.dipper.monitor.service.elastic.template;
//
//import com.alibaba.fastjson.JSONObject;
//import com.dipper.monitor.BaseMonitorTest;
//import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class EsTemplateEntityServiceTest extends BaseMonitorTest {
//
//    @Autowired
//    private ElasticStoreTemplateService elasticStoreTemplateService;
//    @Autowired
//    private ElasticRealTemplateService elasticRealTemplateService;
//
//    /**
//     * {
//     * 	"index_patterns": ["ailpha-baas-flow-yyyyMMdd-*"],
//     * 	"settings": {
//     * 		"index": {
//     * 			"number_of_shards": 4,
//     * 			"number_of_replicas": 0,
//     * 			"translog": {
//     * 				"flush_threshold_size": "1024mb",
//     * 				"retention": {
//     * 					"size": "32mb",
//     * 					"age": "1h"
//     * 				                },
//     * 				"generation_threshold_size": "128mb",
//     * 				"durability": "async"            * 			},
//     * 			"refresh_interval": "60s",
//     * 			"merge": {
//     * 				"policy": {
//     * 					"max_merge_at_once": "30",
//     * 					"segments_per_tier": "60",
//     * 					"floor_segment": "10mb",
//     * 					"max_merged_segment": "1gb"
//     *                },
//     * 				"scheduler": {
//     * 					"max_thread_count": 1,
//     * 					"auto_throttle": true,
//     * 					"max_merge_count": 100
//     *                }
//     *            },
//     * 			"lifecycle": {
//     * 				"name": "securitylog-flow-policy",
//     * 				"rollover_alias": "ailpha-securitylog-flow-yyyyMMdd"
//     *            },
//     * 			"mapping": {
//     * 				"ignore_malformed": true
//     *            },
//     * 			"unassigned": {
//     * 				"node_left": {
//     * 					"delayed_timeout": "10m"
//     *                }
//     *            },
//     * 			"search": {
//     * 				"slowlog": {
//     * 					"level": "info",
//     * 					"threshold": {
//     * 						"query": {
//     * 							"info": "10s"
//     *                        },
//     * 						"fetch": {
//     * 							"info": "5s"
//     *                        }
//     *                    }
//     *                }
//     *            },
//     * 			"indexing": {
//     * 				"slowlog": {
//     * 					"level": "info",
//     * 					"threshold": {
//     * 						"index": {
//     * 							"info": "5s"
//     *                        }
//     *                    }
//     *                }
//     *            }
//     *        }    * 	},
//     * 	"mappings": {
//     * 		"_doc": {
//     * 			"properties": {
//     * 				"example_field": {
//     * 					"type": "text"
//     * 				}
//     * 			}
//     * 		}
//     * 	}    ,
//     * 	"aliases": {
//     * 		"ailpha-securitylog-flow-yyyyMMdd": {
//     * 			"is_write_index": true
//     *        }
//     *    }
//     * }
//     */
//    @Test
//    public void previewTemplate() {
//        String content = "{\"settings\":{\"index\":{\"number_of_shards\":4,\"number_of_replicas\":0,\"translog\":{\"flush_threshold_size\":\"1024mb\",\"retention\":{\"size\":\"32mb\",\"age\":\"1h\"},\"generation_threshold_size\":\"128mb\",\"durability\":\"async\"},\"refresh_interval\":\"60s\",\"merge\":{\"policy\":{\"max_merge_at_once\":\"30\",\"segments_per_tier\":\"60\",\"floor_segment\":\"10mb\",\"max_merged_segment\":\"1gb\"},\"scheduler\":{\"max_thread_count\":1,\"auto_throttle\":true,\"max_merge_count\":100}},\"lifecycle\":{\"name\":\"securitylog-flow-policy\",\"rollover_alias\":\"ailpha-securitylog-flow-yyyyMMdd\"},\"mapping\":{\"ignore_malformed\":true},\"unassigned\":{\"node_left\":{\"delayed_timeout\":\"10m\"}},\"search\":{\"slowlog\":{\"level\":\"info\",\"threshold\":{\"query\":{\"info\":\"10s\"},\"fetch\":{\"info\":\"5s\"}}}},\"indexing\":{\"slowlog\":{\"level\":\"info\",\"threshold\":{\"index\":{\"info\":\"5s\"}}}}}},\"mappings\":{\"_doc\":{\"properties\":{\"example_field\":{\"type\":\"text\"}}}}}";
//
//        EsUnconvertedTemplate esUnconvertedTemplate = new EsUnconvertedTemplate();
//        esUnconvertedTemplate.setEnName("ailpha-log");
//        esUnconvertedTemplate.setZhName("原始日志");
//
//        Map<String, Object> settings = new HashMap<>();
//        settings.put("index.number_of_shards", "31");
//        settings.put("index.refresh_interval", "31s");
//        settings.put("index.number_of_replicas", "31");
//
//        esUnconvertedTemplate.setSettings(settings);
//        esUnconvertedTemplate.setDicName("security_log");
//        esUnconvertedTemplate.setIndexPatterns("ailpha-baas-flow-yyyyMMdd-*");
//        esUnconvertedTemplate.setAliansPatterns("ailpha-securitylog-flow-yyyyMMdd");
//        esUnconvertedTemplate.setNumberOfShards(2);
//        esUnconvertedTemplate.setNumberOfReplicas(4);
//        esUnconvertedTemplate.setEnableAutoShards(true);
//        esUnconvertedTemplate.setTemplateContent(content);
//
////        JSONObject jsonObject = elasticRealTemplateService.previewTemplate(esUnconvertedTemplate);
////        System.out.println(jsonObject.toJSONString());
//    }
//
//    @Test
//    public void addTemplate() {
//        String content = "{\"settings\":{\"index\":{\"number_of_shards\":4,\"number_of_replicas\":0,\"translog\":{\"flush_threshold_size\":\"1024mb\",\"retention\":{\"size\":\"32mb\",\"age\":\"1h\"},\"generation_threshold_size\":\"128mb\",\"durability\":\"async\"},\"refresh_interval\":\"60s\",\"merge\":{\"policy\":{\"max_merge_at_once\":\"30\",\"segments_per_tier\":\"60\",\"floor_segment\":\"10mb\",\"max_merged_segment\":\"1gb\"},\"scheduler\":{\"max_thread_count\":1,\"auto_throttle\":true,\"max_merge_count\":100}},\"lifecycle\":{\"name\":\"securitylog-flow-policy\",\"rollover_alias\":\"ailpha-securitylog-flow-yyyyMMdd\"},\"mapping\":{\"ignore_malformed\":true},\"unassigned\":{\"node_left\":{\"delayed_timeout\":\"10m\"}},\"search\":{\"slowlog\":{\"level\":\"info\",\"threshold\":{\"query\":{\"info\":\"10s\"},\"fetch\":{\"info\":\"5s\"}}}},\"indexing\":{\"slowlog\":{\"level\":\"info\",\"threshold\":{\"index\":{\"info\":\"5s\"}}}}}},\"mappings\":{\"_doc\":{\"properties\":{\"example_field\":{\"type\":\"text\"}}}}}";
//
//        EsUnconvertedTemplate esUnconvertedTemplate = new EsUnconvertedTemplate();
//        esUnconvertedTemplate.setEnName("ailpha-log");
//        esUnconvertedTemplate.setZhName("原始日志");
//
//        Map<String, Object> settings = new HashMap<>();
//        settings.put("index.number_of_shards", "31");
//        settings.put("index.refresh_interval", "31s");
//        settings.put("index.number_of_replicas", "31");
//
//        esUnconvertedTemplate.setSettings(settings);
//        esUnconvertedTemplate.setDicName("security_log");
//        esUnconvertedTemplate.setIndexPatterns("ailpha-baas-flow-yyyyMMdd-*");
//        esUnconvertedTemplate.setAliansPatterns("ailpha-securitylog-flow-yyyyMMdd");
//        esUnconvertedTemplate.setNumberOfShards(2);
//        esUnconvertedTemplate.setNumberOfReplicas(4);
//        esUnconvertedTemplate.setEnableAutoShards(true);
//        esUnconvertedTemplate.setTemplateContent(content);
//
//        elasticStoreTemplateService.addOrUpdateTemplate(esUnconvertedTemplate);
//    }
//
//    @Test
//    public void getTemplate() {
//    }
//
//    @Test
//    public void updateTemplate() {
//    }
//
//
//    /**
//     * 滚动不带时间的模版
//     */
//    @Test
//    public void rollTemplate1() throws Exception {
//        String content = "{\"settings\":{\"index\":{\"number_of_shards\":4,\"number_of_replicas\":0,\"translog\":{\"flush_threshold_size\":\"1024mb\",\"retention\":{\"size\":\"32mb\",\"age\":\"1h\"},\"generation_threshold_size\":\"128mb\",\"durability\":\"async\"},\"refresh_interval\":\"60s\",\"merge\":{\"policy\":{\"max_merge_at_once\":\"30\",\"segments_per_tier\":\"60\",\"floor_segment\":\"10mb\",\"max_merged_segment\":\"1gb\"},\"scheduler\":{\"max_thread_count\":1,\"auto_throttle\":true,\"max_merge_count\":100}},\"lifecycle\":{\"name\":\"securitylog-flow-policy\",\"rollover_alias\":\"ailpha-securitylog-flow-yyyyMMdd\"},\"mapping\":{\"ignore_malformed\":true},\"unassigned\":{\"node_left\":{\"delayed_timeout\":\"10m\"}},\"search\":{\"slowlog\":{\"level\":\"info\",\"threshold\":{\"query\":{\"info\":\"10s\"},\"fetch\":{\"info\":\"5s\"}}}},\"indexing\":{\"slowlog\":{\"level\":\"info\",\"threshold\":{\"index\":{\"info\":\"5s\"}}}}}},\"mappings\":{\"_doc\":{\"properties\":{\"example_field\":{\"type\":\"text\"}}}}}";
//        System.out.println(content);
//
//        EsUnconvertedTemplate esUnconvertedTemplate = new EsUnconvertedTemplate();
//        esUnconvertedTemplate.setEnName("ailpha-log");
//        esUnconvertedTemplate.setZhName("原始日志");
//
//        Map<String, Object> settings = new HashMap<>();
//        settings.put("index.number_of_shards", "31");
//        settings.put("index.refresh_interval", "31s");
//        settings.put("index.number_of_replicas", "31");
//
//        esUnconvertedTemplate.setSettings(settings);
//        esUnconvertedTemplate.setDicName("security_log");
//        esUnconvertedTemplate.setIndexPatterns("ailpha-baas-flow-yyyyMMdd-*");
//        esUnconvertedTemplate.setAliansPatterns("ailpha-securitylog-flow-yyyyMMdd");
//        esUnconvertedTemplate.setNumberOfShards(2);
//        esUnconvertedTemplate.setNumberOfReplicas(4);
//        esUnconvertedTemplate.setEnableAutoShards(true);
//        esUnconvertedTemplate.setTemplateContent(content);
//
////        elasticStoreTemplateService.rollTemplate(esUnconvertedTemplate);
//    }
//
//
//}