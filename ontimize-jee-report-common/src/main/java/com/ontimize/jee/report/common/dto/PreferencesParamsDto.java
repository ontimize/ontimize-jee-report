package com.ontimize.jee.report.common.dto;

public class PreferencesParamsDto {

    private String name;
    private String description;
    private String entity;
    private String service;
    private ReportParamsDto reportParams;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public ReportParamsDto getReportParams() {
        return reportParams;
    }

    public void setReportParams(ReportParamsDto reportParams) {
        this.reportParams = reportParams;
    }

    public PreferencesParamsDto() {
    }

}
