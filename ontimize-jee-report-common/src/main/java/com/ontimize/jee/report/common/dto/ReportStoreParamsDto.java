package com.ontimize.jee.report.common.dto;

import com.ontimize.jee.server.rest.FilterParameter;

import java.util.List;

public class ReportStoreParamsDto {

    private FilterParameter filters;

    private List<ReportStoreParamValueDto> parameters;

    public FilterParameter getFilters() {
        return filters;
    }

    public void setFilters(FilterParameter filters) {
        this.filters = filters;
    }

    public List<ReportStoreParamValueDto> getParameters() {
        return parameters;
    }

    public void setParameters(List<ReportStoreParamValueDto> parameters) {
        this.parameters = parameters;
    }
}
