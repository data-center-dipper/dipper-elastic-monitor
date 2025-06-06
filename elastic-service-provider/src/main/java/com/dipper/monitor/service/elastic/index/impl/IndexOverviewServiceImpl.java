package com.dipper.monitor.service.elastic.index.impl;

import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.entity.db.elastic.IndexWriteEntity;
import com.dipper.monitor.service.elastic.index.FeatureIndexService;
import com.dipper.monitor.service.elastic.index.IndexOverviewService;
import com.dipper.monitor.service.elastic.index.IndexWriteService;
import com.dipper.monitor.utils.mock.MockAllData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class IndexOverviewServiceImpl implements IndexOverviewService {

    @Autowired
    private IndexWriteService indexWriteService;
    @Autowired
    private FeatureIndexService featureIndexService;

    @Override
    public List<IndexWriteEntity> writeIndexList() {
        if(ApplicationUtils.isWindows()){
            return MockAllData.writeIndexList();
        }
        List<IndexWriteEntity> all = indexWriteService.getAll();
        return all;
    }

    @Override
    public List<IndexWriteEntity> featureIndexList() {
        if(ApplicationUtils.isWindows()){
            return MockAllData.writeIndexList();
        }
        List<IndexWriteEntity> all = featureIndexService.featureIndexList();
        return all;
    }

}
