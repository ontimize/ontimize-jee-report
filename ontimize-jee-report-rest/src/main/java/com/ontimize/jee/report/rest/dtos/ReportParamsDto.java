package com.ontimize.jee.report.rest.dtos;

import java.util.List;

public class ReportParamsDto {

	private String title;
	private List<String> columns;
	private String entity;
	private List<String> groups;
	private String service;

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

}
