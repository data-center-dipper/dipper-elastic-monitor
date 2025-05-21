package com.dipper.monitor.service.elastic.index;

import java.io.IOException;
import java.util.List;

public interface IndexBatchOperatorService {

    boolean forceFlushIndexs(String indexs) throws IOException;
    boolean forceClearIndexs(String indexs) throws IOException;
    boolean closeIndexs(String indexs) throws IOException;
    boolean openIndexs(String indexs) throws IOException;
    boolean delIndexs(String indexs) throws IOException;
    boolean frozenIndexs(String indexs) throws IOException;
    boolean unFrozenIndexs(String indexs) throws IOException;

    boolean refreshIndexs(String indexs) throws IOException;
    boolean optimizeIndexs(String indexs, int maxNumSegments) throws IOException;
    boolean rolloverIndex(String aliasName, String newIndexName) throws IOException;

    // 支持 list 输入
    boolean forceFlushIndexs(List<String> indexList) throws IOException;
    boolean forceClearIndexs(List<String> indexList) throws IOException;
    boolean closeIndexs(List<String> indexList) throws IOException;
    boolean openIndexs(List<String> indexList) throws IOException;
    boolean delIndexs(List<String> indexList) throws IOException;
    boolean frozenIndexs(List<String> indexList) throws IOException;
    boolean unFrozenIndexs(List<String> indexList) throws IOException;
    boolean refreshIndexs(List<String> indexList) throws IOException;
    boolean optimizeIndexs(List<String> indexList, int maxNumSegments) throws IOException;
}
