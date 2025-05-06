package com.dipper.monitor.enums;

 public enum MonitorResultEnum {
            SUCCESS(0, "操作成功", "success"),
            SUCCESS_WITH_PARAM(0, "操作成功：%s", "success.with.param"),



    /*  50 */   WARNING_ERROR(-9999, "警告", "warning.error"),
    /*  51 */   UNKNOWN_ERROR(-10000, "操作失败", "unknown.error");



       public int code;
    
       public String msg;
    
       public String name;
    
    
       MonitorResultEnum(int code, String msg, String name) {
        /* 713 */     this.code = code;
        /* 714 */     this.msg = msg;
        /* 715 */     this.name = name;
           }
    
    
    
       public String getMsg() {
        /* 721 */     return this.msg;
           }
     }