package com.dipper.monitor.mapper;


import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
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
}