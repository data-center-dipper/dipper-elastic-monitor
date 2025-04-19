package com.dipper.monitor.entity.db.comon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data // Lombok annotation for automatic generation of getters, setters, equals, hash, and toString methods
@Builder // Lombok annotation to create a builder pattern for the class
@NoArgsConstructor // Lombok annotation to generate a no-args constructor
@AllArgsConstructor // Lombok annotation to generate an all-args constructor
public class ModuleTaskMapEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String clusterCode;

    private String moduleName;

    private String entityName;

    private String sectionName;

    private String taskCode;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}