package com.ontimize.jee.report.rest.dtos;

import java.util.List;

public class PreferencesParamsDto {

	private String name;
	public boolean vertical;
	private String title;
	private String subtitle;
	private List<String> columns;
	private List<String> groups;
	private List<String> functions;
	private List<String> styleFunctions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isVertical() {
		return vertical;
	}

	public void setVertical(boolean vertical) {
		this.vertical = vertical;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public List<String> getFunctions() {
		return functions;
	}

	public void setFunctions(List<String> functions) {
		this.functions = functions;
	}

	public List<String> getStyleFunctions() {
		return styleFunctions;
	}

	public void setStyleFunctions(List<String> styleFunctions) {
		this.styleFunctions = styleFunctions;
	}

}
