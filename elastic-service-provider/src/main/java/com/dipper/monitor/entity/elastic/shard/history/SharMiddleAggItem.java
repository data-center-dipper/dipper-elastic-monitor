package com.dipper.monitor.entity.elastic.shard.history;

import com.dipper.monitor.entity.elastic.index.IndexEntity;
import lombok.Data;

@Data
public class SharMiddleAggItem extends IndexEntity  {
    private String dateTime;
}
