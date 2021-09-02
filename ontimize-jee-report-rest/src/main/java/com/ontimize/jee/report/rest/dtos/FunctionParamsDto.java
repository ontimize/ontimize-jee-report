package com.ontimize.jee.report.rest.dtos;

import java.util.List;

import com.ontimize.jee.common.services.reportstore.ColumnStyleParamsDto;

public class FunctionParamsDto {

	private List<String> columns;
	private String entity;
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

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

}
