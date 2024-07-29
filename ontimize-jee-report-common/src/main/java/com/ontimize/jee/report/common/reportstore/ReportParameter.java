package com.ontimize.jee.report.common.reportstore;

public class ReportParameter {

    /**
     * The name.
     */
    private String reportParameterName;

    /**
     * The description.
     */
    private String reportParameterDescription;

    /**
     * The value class.
     */
    private String reportParameterValueClass;

    /**
     * The nested type.
     */
    private String reportParameterType;

    public ReportParameter(String name, String description, String valueClass, String type) {
        super();
        this.reportParameterName = name;
        this.reportParameterDescription = description;
        this.reportParameterValueClass = valueClass;
        this.reportParameterType = type;
    }

    public String getReportParameterName() {
        return reportParameterName;
    }

    public String getReportParameterDescription() {
        return reportParameterDescription;
    }

    public String getReportParameterValueClass() {
        return reportParameterValueClass;
    }

    public String getReportParameterType() {
        return reportParameterType;
    }

    @Override
    public String toString() {
        return "ReportParameter [reportParameterName=" + reportParameterName + ", reportParameterDescription=" + reportParameterDescription + ", reportParameterValueClass=" + reportParameterValueClass
                + ", reportParameterType=" + reportParameterType + "]";
    }

}
