package com.dipper.monitor.service.elastic.dic;

import com.dipper.monitor.BaseMonitorTest;
import com.dipper.monitor.entity.elastic.dic.Dic;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class DicServiceTest extends BaseMonitorTest {

    @Autowired
    private DicService dicService;

    @Test
    public void addDic() {
        Dic dic = new Dic();
        dic.setEnName("security_log");
        dic.setZhName("安全日志");
        dic.setBusinessAttribute("安全日志,xxx");
        dicService.addDic(dic);
    }

    @Test
    public void updateDic() {
        Dic dic = new Dic();
        dic.setId(1);
        dic.setEnName("security_log");
        dic.setZhName("安全日志1");
        dic.setBusinessAttribute("安全日志,xxx");
        dicService.updateDic(dic);
    }

    @Test
    public void deleteDic() {
        dicService.deleteDic(1);
    }

    @Test
    public void getDic() {
    }

    @Test
    public void getAllDics() {
    }
}