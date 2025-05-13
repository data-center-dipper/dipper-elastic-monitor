package com.dipper.monitor.service.elastic.policy.impl;

import com.dipper.monitor.mapper.LifePolicyStoreMapper;
import com.dipper.monitor.service.elastic.policy.LifePolicyStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LifePolicyStoreServiceImpl implements LifePolicyStoreService {

    @Autowired
    private LifePolicyStoreMapper lifePolicyStoreMapper;
}
