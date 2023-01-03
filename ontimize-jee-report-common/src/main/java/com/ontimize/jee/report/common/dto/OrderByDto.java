package com.ontimize.jee.report.common.dto;

public class OrderByDto {
    private String columnId = null;
    private boolean ascendent = true;

    public OrderByDto() {
        // no-op
    }

    public OrderByDto(String columnId, boolean ascendent) {
        this.columnId = columnId;
        this.ascendent = ascendent;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public boolean isAscendent() {
        return ascendent;
    }

    public void setAscendent(boolean ascendent) {
        this.ascendent = ascendent;
    }

    @Override
    public String toString() {
        return "{'columnId':" + columnId.toString() + ", 'ascendent':" + String.valueOf(ascendent) + "}";
    }

}
