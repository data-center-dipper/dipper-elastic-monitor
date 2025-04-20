package com.dipper.monitor.mapper;


import com.dipper.monitor.entity.elastic.template.EsTemplate;
import org.apache.ibatis.annotations.*;

        import java.util.List;

@Mapper
public interface EsTemplateMapper {


    int insertTemplate(EsTemplate esTemplate);

    EsTemplate getTemplateById(@Param("id") Long id);


    int updateTemplate(EsTemplate esTemplate);


    void deleteTemplateById(@Param("id") Long id);

    List<EsTemplate> getAllTemplates();
}