package com.dipper.monitor.service.elastic.template;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;

/**
 * 模版预览相关
 */
public interface TemplatePreviewService {

    /**
     * 预览生成的模版,这个是模版的模版，还不能直接使用
     * @param esUnconvertedTemplate
     * @return
     */
    JSONObject previewTemplate(EsUnconvertedTemplate esUnconvertedTemplate);

    /**
     * 预览生效的模版，这个是真正可用的模版
     * @param id
     * @return
     */
    JSONObject previewEffectTemplate(Integer id);

    /**
     *
     * @param id
     * @return
     */
    JSONObject previewEffectTemplateByDate(Integer id,String date);
}
