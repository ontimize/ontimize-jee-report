package com.ontimize.jee.report.common.dto.renderer;

import java.util.List;

public class TranslateRendererDto extends RendererDto {

    private String service;
    private String path;
    private String entity;
    private String keyColumn;
    private String valueColumn;
    private List<String> columns;


    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

}
