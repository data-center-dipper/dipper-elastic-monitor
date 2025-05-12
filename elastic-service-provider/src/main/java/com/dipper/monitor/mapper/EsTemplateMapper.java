package com.dipper.monitor.mapper;


import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.template.ElasticTemplateListView;
import org.apache.ibatis.annotations.*;

        import java.util.List;

@Mapper
public interface EsTemplateMapper {


    int insertTemplate(EsTemplateEntity esTemplateEntity);

    EsTemplateEntity getTemplateById(@Param("id") Long id);


    int updateTemplate(EsTemplateEntity esTemplateEntity);


    void deleteTemplateById(@Param("id") Long id);

    List<EsTemplateEntity> getAllTemplates();

    /**
     * 根据英文名称查询模版
     * @param clusterCode
     * @param enName
     * @return
     */
    EsTemplateEntity getTemplateByEnName(@Param("clusterCode") String clusterCode,
                                         @Param("enName") String enName);

    void updateTemplateStat(@Param("id") Long id,@Param("statMessage")  String statMessage);

    Integer getTemplateNum(@Param("clusterCode") String clusterCode,
                           @Param("keyword")  String keyword);

    List<EsTemplateEntity> getTemplateByPage(@Param("clusterCode") String clusterCode,
                                                    @Param("keyword") String keyword,
                                                    @Param("pageSize") Integer pageSize,
                                                    @Param("offset") Integer offset);
}