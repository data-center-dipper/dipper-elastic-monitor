package com.dipper.monitor.service.elastic.alians;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.alians.AliasListView;
import com.dipper.monitor.entity.elastic.alians.AliasPageReq;
import com.dipper.monitor.entity.elastic.alians.IndexAlias;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 别名管理服务接口。
 * 提供对别名的各种操作，包括查询、添加、修改写索引、设置只读等。
 */
public interface ElasticAliasService {

    /**
     * 判断指定的别名数据是否包含多个可写索引（存在冲突）。
     * @param aliasData 别名相关的数据字符串（如 JSON）
     * @return 如果存在多个可写索引，返回 true；否则返回 false
     */
    boolean isWriteEx(String aliasData);

    /**
     * 获取某个别名所指向的最大索引名称（适用于滚动索引场景，例如 logs-001, logs-002）。
     * @param aliasData 别名相关的数据字符串，用于解析关联的索引列表
     * @return 最大的索引名称
     */
    String getAliasMaxIndexRolling(String aliasData);

    /**
     * 更改指定别名的写索引。
     * @param indexMax 新的写索引名称
     * @param alias 当前操作的别名
     * @param b 是否设置为写索引
     * @return 操作结果（通常为 API 返回的 JSON 字符串）
     * @throws Exception 操作失败时抛出异常
     */
    String changeIndexWrite(String indexMax, String alias, boolean b) throws Exception;

    /**
     * 获取所有存在异常的别名列表（比如一个别名指向两个可写索引）。
     * @return 异常别名名称列表
     * @throws IOException 网络或 IO 错误
     */
    Map<String, List<IndexAlias>> listExceptionAlias()  throws IOException;

    List<IndexAlias> getAliasIndexList() throws IOException;

    /**
     * 获取所有别名与索引的映射关系。
     * @return key 是别名名称，value 是该别名关联的一组索引及其写标志信息
     * @throws IOException 网络或 IO 错误
     */
    Map<String, List<IndexAlias>> getAliasIndexMap() throws IOException;

    /**
     * 获取所有别名的原始 JSON 数据。
     * @return key 为别名名称，value 为对应的 JSON 对象
     * @throws IOException 网络或 IO 错误
     */
    Map<String, JSONObject> getAllAliasJson() throws IOException;

    /**
     * 统计指定别名下可写索引的数量。
     * @param aliasResult 别名相关的信息字符串
     * @return 可写索引数量
     */
    int countAliasWrite(String aliasResult);

    /**
     * 根据索引模式匹配查找别名列表。
     * @param indexPatterns 索引模式（支持通配符匹配）
     * @return 匹配的别名列表
     */
    List<String> listAliasByIndexPatterns(String indexPatterns);

    /**
     * 将指定别名设置为只读状态（不可写）。
     * @param alias 要设置的别名
     * @return 操作结果（通常为 API 返回的 JSON 字符串）
     * @throws Exception 操作失败时抛出异常
     */
    String setAliasReadOnly(String alias) throws Exception;

    /**
     * 为指定索引添加一个别名。
     * @param indexName 索引名称
     * @param aliasName 别名名称
     * @return 操作结果（通常为 API 返回的 JSON 字符串）
     * @throws Exception 操作失败时抛出异常
     */
    String addAlias(String indexName, String aliasName) throws Exception;

    /**
     * 获取名称匹配给定模式的所有别名。
     * @param nameLike 模糊匹配的别名名称（可用通配符）
     * @return 匹配的别名列表
     * @throws IOException 网络或 IO 错误
     */
    List<String> aliasNames(String nameLike) throws IOException;

    /**
     * 分页获取别名列表，并附带总数。
     * @param aliasPageReq 分页请求参数
     * @return Tuple2 中的第一个元素为当前页的别名列表（AliasListView 类型），第二个元素为总记录数
     * @throws IOException 网络或 IO 错误
     */
    Tuple2<List<AliasListView>, Long> getAliasByPage(AliasPageReq aliasPageReq) throws IOException;

    List<IndexAlias> aliasCheck() throws IOException;

    List<String> aliasRepair();
}