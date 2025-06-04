package com.dipper.monitor.entity.elastic.template;

import lombok.Data;

@Data
public class AutoShardReq {
  private Integer  id;
  private Integer  numberOfReplicas;
  private Integer  numberOfShards;
  private Integer  shardSize;
  private Boolean  autoShard;
}
