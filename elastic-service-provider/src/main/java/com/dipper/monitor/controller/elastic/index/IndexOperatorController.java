package com.dipper.monitor.controller.elastic.index;

import com.dipper.monitor.service.elastic.index.IndexBatchOperatorService;
import com.dipper.monitor.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 索引批量操作 Controller
 */
@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/index_operator")
@Tag(name = "索引操作管理", description = "提供对 Elasticsearch 索引的批量操作，如刷新、关闭、冻结等")
public class IndexOperatorController {

    @Autowired
    private IndexBatchOperatorService indexBatchOperatorService;

    // ==================== 接口定义 ====================

    /**
     * 强制刷新索引
     */
    @PostMapping("/refresh")
    @Operation(summary = "强制刷新索引", description = "执行 refresh 操作，使文档立即可搜索",
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作结果", content = @Content(schema = @Schema(implementation = Boolean.class)))
            },
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object forceRefresh(@Parameter(description = "索引名称列表", example = "[\"index1\", \"index2\"]") @RequestBody List<String> indices) throws Exception {
        try {
            return ResultUtils.onSuccess(indexBatchOperatorService.forceFlushIndexs(indices));
        }catch (Exception e) {
            log.error("清除索引缓存失败", e);
            return ResultUtils.onFail();
        }
    }

    /**
     * 清除索引缓存
     */
    @PostMapping("/clear_cache")
    @Operation(summary = "清除索引缓存", description = "清空指定索引的查询缓存",
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作结果", content = @Content(schema = @Schema(implementation = Boolean.class)))
            },
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object clearCache(@Parameter(description = "索引名称列表", example = "[\"index1\", \"index2\"]") @RequestBody List<String> indices) throws Exception {
        try {
            return ResultUtils.onSuccess(indexBatchOperatorService.forceClearIndexs(indices));
        }catch (Exception e) {
            log.error("清除索引缓存失败", e);
            return ResultUtils.onFail();
        }
    }

    /**
     * 关闭索引
     */
    @PostMapping("/closeIndexBatch")
    @Operation(summary = "关闭索引", description = "关闭指定的索引",
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作结果", content = @Content(schema = @Schema(implementation = Boolean.class)))
            },
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object closeIndexBatch(@Parameter(description = "索引名称列表", example = "[\"index1\", \"index2\"]") @RequestBody List<String> indices) throws Exception {
        try {
            return ResultUtils.onSuccess(indexBatchOperatorService.closeIndexs(indices));
        }catch (Exception e) {
            log.error("清除索引缓存失败", e);
            return ResultUtils.onFail();
        }
    }

    /**
     * 打开索引
     */
    @PostMapping("/openIndexBatch")
    @Operation(summary = "打开索引", description = "打开已关闭的索引",
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作结果", content = @Content(schema = @Schema(implementation = Boolean.class)))
            },
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object openIndexBatch(@Parameter(description = "索引名称列表", example = "[\"index1\", \"index2\"]") @RequestBody List<String> indices) throws Exception {
        try {
            return ResultUtils.onSuccess(indexBatchOperatorService.openIndexs(indices));
        }catch (Exception e) {
            log.error("清除索引缓存失败", e);
            return ResultUtils.onFail();
        }
    }

    /**
     * 删除索引
     */
    @PostMapping("/deleteIndexBatch")
    @Operation(summary = "删除索引", description = "删除指定的索引",
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作结果", content = @Content(schema = @Schema(implementation = Boolean.class)))
            },
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object deleteIndexBatch(@Parameter(description = "索引名称列表", example = "[\"index1\", \"index2\"]") @RequestBody List<String> indiceNames) throws Exception {
        try {
            return ResultUtils.onSuccess(indexBatchOperatorService.delIndexs(indiceNames));
        }catch (Exception e) {
            log.error("清除索引缓存失败", e);
            return ResultUtils.onFail();
        }
    }

    /**
     * 冻结索引
     */
    @PostMapping("/freeze")
    @Operation(summary = "冻结索引", description = "冻结指定的索引，使其只读",
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作结果", content = @Content(schema = @Schema(implementation = Boolean.class)))
            },
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object freezeIndex(@Parameter(description = "索引名称列表", example = "[\"index1\", \"index2\"]") @RequestBody List<String> indices) throws Exception {
        try {
            return ResultUtils.onSuccess(indexBatchOperatorService.frozenIndexs(indices));
        }catch (Exception e) {
            log.error("清除索引缓存失败", e);
            return ResultUtils.onFail();
        }
    }

    /**
     * 解冻索引
     */
    @PostMapping("/unfreeze")
    @Operation(summary = "解冻索引", description = "解冻指定的索引",
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作结果", content = @Content(schema = @Schema(implementation = Boolean.class)))
            },
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object unfreezeIndex(@Parameter(description = "索引名称列表", example = "[\"index1\", \"index2\"]") @RequestBody List<String> indices) throws Exception {
        try {
            return ResultUtils.onSuccess(indexBatchOperatorService.unFrozenIndexs(indices));
        }catch (Exception e) {
            log.error("清除索引缓存失败", e);
            return ResultUtils.onFail();
        }
    }

    /**
     * 强制合并段（optimize）
     */
    @PostMapping("/forcemerge")
    @Operation(summary = "强制合并段", description = "合并索引段以优化性能",
            parameters = {
                    @Parameter(name = "maxNumSegments", in = ParameterIn.QUERY, description = "目标段数", required = true, schema = @Schema(type = "integer", example = "1"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作结果", content = @Content(schema = @Schema(implementation = Boolean.class)))
            },
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object optimizeIndex(
            @Parameter(description = "索引名称列表", example = "[\"index1\", \"index2\"]") @RequestBody List<String> indices,
            @RequestParam int maxNumSegments) throws Exception {
        try {
            return ResultUtils.onSuccess(indexBatchOperatorService.optimizeIndexs(indices, maxNumSegments));
        }catch (Exception e) {
            log.error("清除索引缓存失败", e);
            return ResultUtils.onFail();
        }

    }

    /**
     * 滚动索引（Rollover）
     */
    @PostMapping("/rollover")
    @Operation(summary = "滚动索引", description = "使用 rollover API 创建新索引并更新别名",
            parameters = {
                    @Parameter(name = "aliasName", in = ParameterIn.QUERY, description = "别名", required = true),
                    @Parameter(name = "newIndexName", in = ParameterIn.QUERY, description = "新索引名", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作结果", content = @Content(schema = @Schema(implementation = Boolean.class)))
            },
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object rolloverIndex(
            @RequestParam String aliasName,
            @RequestParam String newIndexName) throws Exception {
        try {
            return ResultUtils.onSuccess(indexBatchOperatorService.rolloverIndex(aliasName, newIndexName));
        }catch (Exception e) {
            log.error("清除索引缓存失败", e);
            return ResultUtils.onFail();
        }
    }
}