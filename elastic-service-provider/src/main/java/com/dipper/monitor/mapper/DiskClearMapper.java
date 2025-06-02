package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.db.elastic.DiskClearItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 磁盘清理配置 Mapper 接口
 */
public interface DiskClearMapper {

    /**
     * 新增一条磁盘清理配置记录
     *
     * @param item 配置数据
     * @return 影响行数
     */
    int insert(DiskClearItem item);

    /**
     * 根据主键删除一条记录
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 更新一条磁盘清理配置记录
     *
     * @param item 配置数据
     * @return 影响行数
     */
    int update(DiskClearItem item);

    /**
     * 根据主键查询一条记录
     *
     * @param id 主键ID
     * @return DiskClearItem 对象
     */
    DiskClearItem selectById(Integer id);

    /**
     * 分页查询所有磁盘清理配置记录
     *
     * @param offset   偏移量（起始位置）
     * @param pageSize 每页数量
     * @return 当前页的数据列表
     */
    List<DiskClearItem> selectByPage(@Param("offset") Integer offset,
                                     @Param("pageSize") Integer pageSize);

    /**
     * 查询所有磁盘清理配置的总记录数
     *
     * @return 总记录数
     */
    int countAll();

    // 分页模糊查询
    List<DiskClearItem> selectByPageWithKeyword(
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize,
            @Param("keyWord") String keyWord);

    // 查询符合条件的总数
    int countByKeyWord(@Param("keyWord") String keyWord);
}