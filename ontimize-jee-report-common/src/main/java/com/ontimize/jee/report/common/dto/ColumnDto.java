package com.ontimize.jee.report.common.dto;

public class ColumnDto {
    String id;
    String name;
    ColumnStyleParamsDto columnStyle;

    public ColumnDto() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnStyleParamsDto getColumnStyle() {
        return columnStyle;
    }

    public void setColumnStyle(ColumnStyleParamsDto columnStyle) {
        this.columnStyle = columnStyle;
    }

}
