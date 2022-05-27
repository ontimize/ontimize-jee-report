package com.ontimize.jee.report.common.dto;

public class OrderByDto {

    private String columnName = null;

    private boolean ascendent = true;

    public OrderByDto() {
        //no-op
    }

    public OrderByDto(String columnName) {
        this.columnName = columnName;
    }

    public OrderByDto(String columnName, boolean ascendent) {
        this.columnName = columnName;
        this.ascendent = ascendent;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setAscendent(boolean ascendent) {
        this.ascendent = ascendent;
    }

    public boolean isAscendent() {
        return this.ascendent;
    }
}
