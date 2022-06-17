package com.ontimize.jee.report.common.reportstore;

public class ReportParameter {

    /**
     * The name.
     */
    private String name;

    /**
     * The description.
     */
    private String description;

    /**
     * The value class.
     */
    private String valueClass;

    /**
     * The nested type.
     */
    private String type;

    public ReportParameter(String name, String description, String valueClass, String type) {
        super();
        this.name = name;
        this.description = description;
        this.valueClass = valueClass;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getValueClass() {
        return valueClass;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ReportParameter [name=" + name + ", description=" + description + ", valueClass=" + valueClass
                + ", type=" + type + "]";
    }

}
