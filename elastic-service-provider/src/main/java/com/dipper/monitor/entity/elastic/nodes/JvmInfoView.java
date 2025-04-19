package com.dipper.monitor.entity.elastic.nodes;

import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.JvmInfo;
import com.dipper.monitor.utils.UnitUtils;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class JvmInfoView {

    private String startTime;
    private String heapInitInGb;
    private String heapMaxInGb;
    private String nonHeapMaxInGb;
    private String directMaxInGb;

    public void transToView(JvmInfo jvmInfo) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.startTime = sdf.format(new Date(jvmInfo.getStartTimeInMillis()));

        this.heapInitInGb = UnitUtils.transToGbOrMB(jvmInfo.getHeapInitInBytes());
        this.heapMaxInGb = UnitUtils.transToGbOrMB(jvmInfo.getHeapMaxInBytes());
        this.nonHeapMaxInGb = UnitUtils.transToGbOrMB(jvmInfo.getNonHeapMaxInBytes());
        this.directMaxInGb = UnitUtils.transToGbOrMB(jvmInfo.getDirectMaxInBytes());
    }
}
