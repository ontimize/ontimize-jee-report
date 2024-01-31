package com.ontimize.jee.report.common.dto;

import com.ontimize.jee.server.rest.FilterParameter;

import java.util.List;

public class ReportStoreParamValueDto {

    private String name;
    
    private Object value;
    
    private Integer sqlType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Integer getSqlType() {
        return sqlType;
    }

    public void setSqlType(Integer sqlType) {
        this.sqlType = sqlType;
    }
}
