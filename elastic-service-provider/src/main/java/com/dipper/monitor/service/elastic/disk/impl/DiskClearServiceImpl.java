package com.dipper.monitor.service.elastic.disk.impl;

import com.dipper.monitor.controller.elastic.template.TemplateStoreController;
import com.dipper.monitor.entity.db.config.ConfItemEntity;
import com.dipper.monitor.entity.db.elastic.DiskClearItem;
import com.dipper.monitor.entity.elastic.disk.clear.DiskClearItemReq;
import com.dipper.monitor.entity.elastic.disk.clear.DiskClearPageReq;
import com.dipper.monitor.entity.elastic.disk.clear.DiskClearView;
import com.dipper.monitor.entity.elastic.disk.GlobalDiskClearReq;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.enums.props.PropsEnum;
import com.dipper.monitor.mapper.DiskClearMapper;
import com.dipper.monitor.service.config.PropsService;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.disk.DiskClearService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class DiskClearServiceImpl implements DiskClearService {

    @Autowired
    private PropsService propsService;
    @Autowired
    private ElasticRealNodeService elasticRealNodeService;
    @Autowired
    private DiskClearMapper diskClearMapper;
    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;
    @Autowired
    private ElasticClusterManagerService elasticClusterManagerService;

    @Override
    public void globalDiskClear(GlobalDiskClearReq globalDiskClearReq) {

        ConfItemEntity item = new ConfItemEntity(PropsEnum.ES_DISK_CLEAR_LOW);
        item.setConfigValue(globalDiskClearReq.getLowThreshold() + "");
        propsService.saveOrUpdate(item);

        ConfItemEntity itemMiddle = new ConfItemEntity(PropsEnum.ES_DISK_CLEAR_MIDDLE);
        itemMiddle.setConfigValue(globalDiskClearReq.getMediumThreshold() + "");
        propsService.saveOrUpdate(itemMiddle);

        ConfItemEntity itemHigh = new ConfItemEntity(PropsEnum.ES_DISK_CLEAR_HIGH);
        itemHigh.setConfigValue(globalDiskClearReq.getHighThreshold() + "");
        propsService.saveOrUpdate(itemHigh);
    }

    @Override
    public List<ElasticNodeDisk> nodeDiskTop10() throws IOException {
        List<ElasticNodeDisk> elasticNodeDisks = elasticRealNodeService.nodeDiskTop10();
        return elasticNodeDisks;
    }

    @Override
    public Tuple2<Integer, List<DiskClearView>> templateDiskClearPage(DiskClearPageReq diskClearPageReq) {
        if (diskClearPageReq.getPageNum() == null || diskClearPageReq.getPageSize() == null) {
            return Tuple2.of(0, Collections.emptyList());
        }

        String keyWord = diskClearPageReq.getKeyWord() == null ? "" : diskClearPageReq.getKeyWord();

        int pageNum = diskClearPageReq.getPageNum();
        int pageSize = diskClearPageReq.getPageSize();

        int offset = (pageNum - 1) * pageSize;

        // 查询总数用于前端分页计算
        int total = diskClearMapper.countByKeyWord(keyWord);

        // 分页查询数据
        List<DiskClearItem> list = diskClearMapper.selectByPageWithKeyword(offset, pageSize, keyWord);

        List<DiskClearView> views = new ArrayList<>();
        for (DiskClearItem item : list) {
            DiskClearView diskClearView = new DiskClearView();
            BeanUtils.copyProperties(item, diskClearView);
            views.add(diskClearView);
        }

        return Tuple2.of(total, views);
    }

    @Override
    public void templateDiskSaveOrUpdate(DiskClearItemReq diskClearItemReq) {
        String templateName = diskClearItemReq.getTemplateName();
        if (StringUtils.isBlank(templateName)) {
            throw new IllegalArgumentException("templateName is blank");
        }
        EsUnconvertedTemplate esUnconvertedTemplate = elasticStoreTemplateService.getOneUnconvertedTemplateByEnName(templateName);
        if (esUnconvertedTemplate == null) {
            throw new IllegalArgumentException("templateName is not exist");
        }
        Date date = new Date();
        Integer id = diskClearItemReq.getId();
        if (id == null) {
            DiskClearItem item = new DiskClearItem();
            BeanUtils.copyProperties(diskClearItemReq, item);
            item.setCreateTime(date);
            item.setUpdateTime(date);
            diskClearMapper.insert(item);
        } else {
            DiskClearItem item = diskClearMapper.selectById(id);
            if (item == null) {
                throw new IllegalArgumentException("id is not exist");
            }
            BeanUtils.copyProperties(diskClearItemReq, item);
            item.setUpdateTime(date);
            diskClearMapper.update(item);
        }
    }

    @Override
    public void templateDiskDelete(Integer id) {
        if (id == null) {
           throw new IllegalArgumentException("id is not exist");
        }
        DiskClearItem item = diskClearMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("id is not exist");
        }
        diskClearMapper.deleteById(id);
    }


}
