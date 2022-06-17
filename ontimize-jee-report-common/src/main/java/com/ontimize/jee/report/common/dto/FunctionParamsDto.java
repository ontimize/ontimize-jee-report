package com.ontimize.jee.report.common.dto;

import java.util.List;

public class FunctionParamsDto {

    private List<String> columns;
    private String entity;
    private String service;
    private String language;

    public List<String> getColumns() {
        return columns;
    }

    public void setColums(List<String> columns) {
        this.columns = columns;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String table) {
        this.entity = table;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
