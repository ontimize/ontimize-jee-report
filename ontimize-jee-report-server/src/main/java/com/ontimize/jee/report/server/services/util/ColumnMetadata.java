package com.ontimize.jee.report.server.services.util;

public class ColumnMetadata {

    private String id;
    private int type;
    private String className;

    public ColumnMetadata() {
        //no-op
    }

    public ColumnMetadata(String id, int type, String className) {
        this.id = id;
        this.type = type;
        this.className = className;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
