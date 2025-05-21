package com.dipper.monitor.controller.elastic.dic;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.dic.Dic;
import com.dipper.monitor.entity.elastic.dic.DicPageInfo;
import com.dipper.monitor.service.elastic.dic.DicService;
import com.dipper.monitor.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/elastic_dic")
public class DicController {

    @Autowired
    private DicService dicService;

    @Operation(summary = "添加字典",
            description = "Add a new dictionary.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Dictionary added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Dic.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/addDic")
    public JSONObject addDic(@RequestBody Dic dic) {
        try {
            Dic addedDic = dicService.addDic(dic);
            if (addedDic == null) {
                return ResultUtils.onFail("Failed to add dictionary");
            }
            return ResultUtils.onSuccess(addedDic);
        } catch (Exception e) {
            log.error("Error adding dictionary", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "更新字典",
            description = "Update an existing dictionary by ID.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Dictionary updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Dic.class))),
                    @ApiResponse(responseCode = "404", description = "Dictionary not found"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PutMapping("/updateDic")
    public JSONObject updateDic(@RequestBody Dic dic) {
        try {
            Dic updatedDic = dicService.updateDic(dic);
            if (updatedDic == null) {
                return ResultUtils.onFail("Dictionary not found");
            }
            return ResultUtils.onSuccess(updatedDic);
        } catch (Exception e) {
            log.error("Error updating dictionary", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "删除字典",
            description = "Delete a dictionary by ID.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Dictionary deleted successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @DeleteMapping("/deleteDic")
    public JSONObject deleteDic(Integer id) {
        try {
            dicService.deleteDic(id);
            return ResultUtils.onSuccess(null);
        } catch (Exception e) {
            log.error("Error deleting dictionary", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "获取字典详情",
            description = "Retrieve a dictionary by ID.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Dic.class))),
                    @ApiResponse(responseCode = "404", description = "Dictionary not found"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/getDic")
    public JSONObject getDic(Integer dicId) {
        try {
            Dic dic = dicService.getDic(dicId);
            if (dic == null) {
                return ResultUtils.onFail("Dictionary not found");
            }
            return ResultUtils.onSuccess(dic);
        } catch (Exception e) {
            log.error("Error retrieving dictionary", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "获取所有字典列表",
            description = "Retrieve a list of all dictionaries.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Dic.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/getAllDics")
    public JSONObject getAllDics(@RequestBody DicPageInfo dicPageInfo) {
        try {
            Integer total = dicService.getDicNum(dicPageInfo);
            List<Dic> dics = dicService.getDicByPage(dicPageInfo);
            return ResultUtils.onSuccessWithPageTotal(total,dics);
        } catch (Exception e) {
            log.error("Error retrieving dictionaries", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "获取所有字典列表",
            description = "Retrieve a list of all dictionaries.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Dic.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/getAllDicsNoPage")
    public JSONObject getAllDics() {
        try {
            List<Dic> dics = dicService.getAllDics();
            return ResultUtils.onSuccess(dics);
        } catch (Exception e) {
            log.error("Error retrieving dictionaries", e);
            return ResultUtils.onFail("Operation error");
        }
    }
}