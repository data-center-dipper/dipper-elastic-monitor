package com.dipper.monitor.entity.elastic.nodes;

import com.dipper.monitor.entity.elastic.original.nodes.info.nodes.JvmInfo;
import com.dipper.monitor.entity.elastic.original.nodes.info.nodes.OsInfo;
import com.dipper.monitor.entity.elastic.original.nodes.stats.JVM;
import com.dipper.monitor.entity.elastic.original.nodes.stats.OS;
import lombok.Data;

@Data
public class OsInfoView {

    private OsInfo osInfo;

    private  OS osStat;

    public void transToView(OsInfo osInfo, OS osStat) {
        this.osInfo = osInfo;
        this.osStat = osStat;
    }
}
