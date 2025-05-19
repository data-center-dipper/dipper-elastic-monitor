package com.dipper.monitor.service.elastic.fastapi;

import com.dipper.monitor.entity.elastic.fastapi.FastApiDefView;

import java.util.List;

public interface FastApiService {
    /**
     * 获取fastapi列表
     * @param nameLike
     * @return
     */
    List<FastApiDefView> fastApiList(String nameLike);

    /**
     * 转换为curl命令
     * @param fastApiDefView
     * @return
     */
    String transToCurl(FastApiDefView fastApiDefView);

    /**
     * 执行fastapi
     * @param fastApiDefView
     * @return
     */
    String executeFastApi(FastApiDefView fastApiDefView);
}
