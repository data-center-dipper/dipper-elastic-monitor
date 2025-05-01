package com.dipper.monitor.entity.elastic.life;

import lombok.Data;

@Data
public class EsLifeCycleManagement {
    private String index;
   private String message;

   public EsLifeCycleManagement() {

   }

    public EsLifeCycleManagement(String index, String message) {
        this.index = index;
        this.message = message;
    }
}
