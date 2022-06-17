package com.ontimize.jee.report.common.dto;

public class FunctionTypeDto {
    String columnName;
    Type type;

    public enum Type {SUM, AVERAGE, MAX, MIN, TOTAL}

    ;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public FunctionTypeDto() {

    }

}
