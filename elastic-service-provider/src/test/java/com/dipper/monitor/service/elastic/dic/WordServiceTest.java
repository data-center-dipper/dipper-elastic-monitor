package com.dipper.monitor.service.elastic.dic;

import com.dipper.monitor.BaseMonitorTest;
import com.dipper.monitor.entity.elastic.dic.Field;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class WordServiceTest extends BaseMonitorTest {

    @Autowired
    private WordService wordService;

    @Test
    public void addField() {
        Field field = new Field();
        field.setDicId(1);
        field.setEnName("test");
        field.setZhName("test");
        field.setFieldType("text");
        field.setEsMappingType("text");

        wordService.addField(field);
    }

    @Test
    public void updateField() {
    }

    @Test
    public void deleteField() {
    }

    @Test
    public void getField() {
    }

    @Test
    public void getFieldsByDicId() {
    }

    @Test
    public void deleteWordsByDicId() {
    }
}