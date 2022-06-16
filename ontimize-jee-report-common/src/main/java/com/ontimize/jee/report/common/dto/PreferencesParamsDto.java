package com.ontimize.jee.report.common.dto;

import java.util.List;
import java.util.Objects;

public class PreferencesParamsDto {

	private String name;
	private String description;
	public boolean vertical;
	private String title;
	private String subtitle;
	private List<String> groups;
	private List<String> functions;
	private StyleParamsDto style;
	private List<ColumnDto> columns;
	private List<OrderByDto> orderBy;
	private String entity;

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

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
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

	public PreferencesParamsDto() {
	}

}
