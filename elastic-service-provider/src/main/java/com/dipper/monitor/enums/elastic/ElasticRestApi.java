package com.dipper.monitor.enums.elastic;

/**
 * 定义Elasticsearch REST API接口及其相关信息。
 */
public enum ElasticRestApi {

    /**
     * 获取Elasticsearch的基本统计信息。
     */
    ES_STAT("GET", "_stats/docs,store?ignore_unavailable=true", "获取ES基本的统计信息"),
    SHAED_LIST("GET", "/_cat/shards?format=json", "获取所有的分片列表"),
    /**
     * 获取Elasticsearch节点的相关信息。
     */
    ES_NODES_STAT_MESSAGE("GET", "/_nodes/stats", "获取ES节点信息"),
    SHAED_UNASSIGNED_REASON("GET", "/_cluster/allocation/explain", "查看分配未分配的原因"),
    SHAED_FORCE_DISTRIBUTION("POST", "/_cluster/reroute?retry_failed=true", "分片重新分配"),
    /**
     * 获取所有节点的简单信息。
     */
    NODES_SIMPLE_LIST("GET", "/_cat/manager_nodes?format=json", "获取所有节点的简单信息"),
    LIFE_CYCLE_STATUE("GET", "_ilm/status", "生命周期管理运行状态"),

    /**
     * 生命周期管理解释API。
     */
    LIFE_CYCLE_MANAGEMENT("GET", "/*/_ilm/explain?pretty", "生命周期管理的API"),

    /**
     * 查看生命周期管理状态。
     */
    LIFE_CYCLE_STATUS("GET", "_ilm/status", "查看生命周期管理运行状态"),

    /**
     * 尝试重新处理失败的索引生命周期阶段。
     */
    LIFE_CYCLE_RETRY("POST", "/{prefix}/_ilm/retry", "尝试重新处理失败的索引生命周期阶段"),

    /**
     * 启动生命周期管理服务。
     */
    LIFE_CYCLE_START("POST", "/_ilm/start", "启动生命周期管理服务"),

    /**
     * 解释未分配分片的原因。
     */
    SHARD_UNASSIGNED_REASON("GET", "/_cluster/allocation/explain", "解释未分配分片的原因"),

    /**
     * 强制重新分配分片。
     */
    SHARD_FORCE_DISTRIBUTION("POST", "/_cluster/reroute?retry_failed=true", "强制重新分配分片"),

    /**
     * 获取分片存储信息。
     */
    SHARD_STORES("GET", "_shard_stores?pretty", "获取分片存储信息"),

    /**
     * 获取所有分片列表。
     */
    SHARD_LIST("GET", "/_cat/shards?format=json", "获取所有分片列表"),

    /**
     * 获取别名列表。
     */
    ALIASES_LIST("GET", "/_cat/aliases", "获取别名列表"),

    /**
     * 添加别名。
     */
    ADD_ALIASES("POST", "/_aliases", "添加别名"),

    /**
     * 获取所有的段信息。
     */
    SEGMENT_LIST("GET", "/_cat/segments?bytes=kb&format=json", "获取所有段信息"),

    /**
     * 强制刷新集群。
     */
    FLUSH_SYNC("POST", "/_flush/synced", "强制刷新集群"),

    /**
     * 清除Shard缓存。
     */
    CLEAR_CACHE("POST", "/_cache/clear", "清除Shard缓存"),

    /**
     * 冻结某个索引。
     */
    INDEX_FREEZE("POST", "/{index}/_freeze", "冻结指定索引"),

    /**
     * 查看集群配置。
     */
    CLUSTER_SETTINGS("GET", "/_cluster/settings", "查看集群配置"),

    /**
     * 查看集群健康状况。
     */
    CLUSTER_HEALTH("GET", "/_cat/health?format=json", "查看集群健康状况"),

    /**
     * 关闭整个集群。
     */
    CLUSTER_SHUTDOWN("POST", "/_cluster/manager_nodes/_shutdown", "关闭整个集群"),

    /**
     * 获取集群文档数量。
     */
    DOCUMENT_COUNT("GET", "/_cat/count?format=json", "获取集群文档数量"),

    /**
     * 获取Elasticsearch插件信息。
     */
    PLUGIN_INFO("GET", "/_cat/plugins?format=json", "获取ES插件信息"),

    /**
     * 查看索引列表。
     */
    INDEX_LIST("GET", "/_cat/indices?bytes=k&format=json", "查看索引列表"),

    /**
     * 查看匹配到的所有索引的磁盘占用情况。
     */
    INDEX_STORE_SIZE("GET", "/_cat/indices/%s?bytes=b&format=json", "查看模式匹配的所有索引磁盘占用"),

    /**
     * 查看索引占用的磁盘信息。
     */
    INDEX_DISK_INFO("GET", "/_cat/allocation?format=json", "查看索引占用的磁盘信息"),

    /**
     * 查看集群UUID。
     */
    CLUSTER_UUID("GET", "/?filter_path=cluster_uuid", "查看集群UUID"),
    CLUSTER_SETTING("GET", "/_cluster/settings", "查看集群的配置"),
    CLUSTER_DOCUMENT_COUNT("GET", "/_cat/count?format=json", "es集群的文档数量"),
    CLUSTER_PLUGIN("GET", "/_cat/plugins?format=json", "获取ES的插件信息"),
    INDEX_GET_STORE_SIZE("GET", "/_cat/indices/%s?bytes=b&format=json", "查看pattern匹配到所有索引的磁盘占用和"),
    INDEX_DISK_MESSAGE("GET", "/_cat/allocation", "查看索引占用的磁盘信息"),
    INDEX_DISK_MESSAGE_JSON("GET", "/_cat/allocation?format=json", "查看索引占用的磁盘信息");

    private final String method;
    private final String apiPath;
    private final String description;

    /**
     * 构造函数初始化REST API类型、API路径及描述信息。
     *
     * @param method HTTP请求方法。
     * @param apiPath API路径。
     * @param description API功能描述。
     */
    ElasticRestApi(String method, String apiPath, String description) {
        this.method = method;
        this.apiPath = apiPath;
        this.description = description;
    }

    /**
     * 获取HTTP请求方法。
     *
     * @return 返回HTTP请求方法。
     */
    public String getMethod() {
        return method;
    }

    /**
     * 获取API路径。
     *
     * @return 返回API路径。
     */
    public String getApiPath() {
        return apiPath;
    }

    /**
     * 获取API的功能描述。
     *
     * @return 返回API的功能描述。
     */
    public String getDescription() {
        return description;
    }
}