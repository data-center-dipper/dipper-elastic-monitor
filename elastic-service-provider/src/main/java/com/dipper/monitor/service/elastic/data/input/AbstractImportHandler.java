package com.dipper.monitor.service.elastic.data.input;

import com.dipper.monitor.entity.elastic.data.ImportDataReq;

public abstract class AbstractImportHandler implements ImportHandler  {

    protected ImportDataReq importDataReq;
    protected String format;
    protected String filePath;

    public AbstractImportHandler(ImportDataReq importDataReq) {
        this.importDataReq  = importDataReq;
        this.format = importDataReq.getFormat();
        this.filePath = importDataReq.getFilePath();
    }

    public abstract void process();
        public abstract void processLine(String line);
    }