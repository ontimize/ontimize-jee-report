package com.ontimize.jee.report.common.dto;

import java.util.List;

import com.ontimize.jee.server.rest.FilterParameter;

public class ReportParamsDto {

    private String title;
    private String entity;
    private List<String> groups;
    private List<OrderByDto> orderBy;
    private String service;
    private String path;
    private Boolean vertical;
    private List<FunctionTypeDto> functions;
    private StyleParamsDto style;
    private String subtitle;
    private List<ColumnDto> columns;
    private String language;
    private FilterParameter filters;
    private Boolean advQuery;

    public String getEntity() {
        return entity;
    }

    public void setEntity(String table) {
        this.entity = table;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getVertical() {
        return vertical;
    }

    public void setVertical(Boolean vertical) {
        this.vertical = vertical;
    }

    public List<FunctionTypeDto> getFunctions() {
        return functions;
    }

    public void setFunctions(List<FunctionTypeDto> functions) {
        this.functions = functions;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public StyleParamsDto getStyle() {
        return style;
    }

    public void setStyle(StyleParamsDto style) {
        this.style = style;
    }

    public List<ColumnDto> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDto> columns) {
        this.columns = columns;
    }

    public List<OrderByDto> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<OrderByDto> orderBy) {
        this.orderBy = orderBy;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public FilterParameter getFilters() {
        return filters;
    }

    public void setFilters(FilterParameter filters) {
        this.filters = filters;
    }

    public Boolean getAdvQuery() {
        return advQuery;
    }

    public void setAdvQuery(Boolean advQuery) {
        this.advQuery = advQuery;
    }

}
