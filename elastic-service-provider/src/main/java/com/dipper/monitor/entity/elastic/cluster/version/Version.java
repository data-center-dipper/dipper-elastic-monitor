package com.dipper.monitor.entity.elastic.cluster.version;

import lombok.Data;

@Data
public  class Version {
    private String number;
    private String buildFlavor;
    private String buildType;
    private String buildHash;
    private String buildDate;
    private boolean buildSnapshot;
    private String luceneVersion;
    private String minimumWireCompatibilityVersion;
    private String minimumIndexCompatibilityVersion;
}