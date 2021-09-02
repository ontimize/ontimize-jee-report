package com.ontimize.jee.report.rest.dtos;

import java.util.List;

import com.ontimize.jee.common.services.reportstore.ColumnStyleParamsDto;

public class ReportParamsDto {

	private String title;
	private List<String> columns;
	private String entity;
	private List<String> groups;
	private String service;
	private String orientation;
	private List<String> functions;
	private List<String> styleFunctions;
	private String subtitle;
	private List<ColumnStyleParamsDto> columnStyle;

	public List<String> getColumns() {
		return columns;
	}

	public void setColums(List<String> columns) {
		this.columns = columns;
	}

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

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public List<String> getFunctions() {
		return functions;
	}

	public void setFunctions(List<String> functions) {
		this.functions = functions;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public List<String> getStyleFunctions() {
		return styleFunctions;
	}

	public void setStyleFunctions(List<String> styleFunctions) {
		this.styleFunctions = styleFunctions;
	}

	public List<ColumnStyleParamsDto> getColumnStyle() {
		return columnStyle;
	}

	public void setColumnStyle(List<ColumnStyleParamsDto> columnStyle) {
		this.columnStyle = columnStyle;
	}

}
