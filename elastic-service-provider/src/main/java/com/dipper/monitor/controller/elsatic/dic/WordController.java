package com.dipper.monitor.controller.elsatic.dic;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.dic.Field;
import com.dipper.monitor.service.elastic.dic.DicService;
import com.dipper.monitor.service.elastic.dic.WordService;
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
@RequestMapping("/dipper/monitor/api/v1/elastic/elastic_word")
public class WordController {

    @Autowired
    private WordService wordService;

    @Operation(summary = "添加字段",
            description = "Add a new field to the dictionary.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Field added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Field.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/addField")
    public JSONObject addField(@RequestBody Field field) {
        try {
            Field addedField = wordService.addField(field);
            if (addedField == null) {
                return ResultUtils.onFail("Failed to add field");
            }
            return ResultUtils.onSuccess(addedField);
        } catch (Exception e) {
            log.error("Error adding field", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "批量添加字段",
            description = "Add a new field to the dictionary.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Field added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Field.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/addFields")
    public JSONObject addFields(@RequestBody List<Field> fields) {
        try {
            wordService.addFields(fields);
            return ResultUtils.onSuccess();
        } catch (Exception e) {
            log.error("Error adding field", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "更新字段",
            description = "Update an existing field by ID.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Field updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Field.class))),
                    @ApiResponse(responseCode = "404", description = "Field not found"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PutMapping("/{id}")
    public JSONObject updateField(@PathVariable("id") Integer id, @RequestBody Field field) {
        try {
            field.setId(id);
            Field updatedField = wordService.updateField(field);
            if (updatedField == null) {
                return ResultUtils.onFail("Field not found");
            }
            return ResultUtils.onSuccess(updatedField);
        } catch (Exception e) {
            log.error("Error updating field", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "删除字段",
            description = "Delete a field by ID.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Field deleted successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @DeleteMapping("/{id}")
    public JSONObject deleteField(@PathVariable("id") Integer id) {
        try {
            wordService.deleteField(id);
            return ResultUtils.onSuccess(null);
        } catch (Exception e) {
            log.error("Error deleting field", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "获取字段详情",
            description = "Retrieve a field by ID.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Field.class))),
                    @ApiResponse(responseCode = "404", description = "Field not found"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/{id}")
    public JSONObject getField(@PathVariable("id") Integer id) {
        try {
            Field field = wordService.getField(id);
            if (field == null) {
                return ResultUtils.onFail("Field not found");
            }
            return ResultUtils.onSuccess(field);
        } catch (Exception e) {
            log.error("Error retrieving field", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "通过字典ID获取字段列表",
            description = "Retrieve a list of fields associated with a specific dictionary ID.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Field.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/byDicId/{dicId}")
    public JSONObject getFieldsByDicId(@PathVariable("dicId") Integer dicId) {
        try {
            List<Field> fields = wordService.getFieldsByDicId(dicId);
            return ResultUtils.onSuccess(fields);
        } catch (Exception e) {
            log.error("Error retrieving fields by dictionary ID", e);
            return ResultUtils.onFail("Operation error");
        }
    }
}
