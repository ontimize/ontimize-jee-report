package com.ontimize.jee.report.common.dto;

import java.util.List;

public class ServiceRendererDto {
    
    private String service;
    private String entity;
    private String keyColumn;
    private String valueColumn;
    private List<String> columns;
    private List<String> parentKeys;
    

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    public void setKeyColumn(String keyColumn) {
        this.keyColumn = keyColumn;
    }

    public String getValueColumn() {
        return valueColumn;
    }

    public void setValueColumn(String valueColumn) {
        this.valueColumn = valueColumn;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getParentKeys() {
        return parentKeys;
    }

    public void setParentKeys(List<String> parentKeys) {
        this.parentKeys = parentKeys;
    }
}
