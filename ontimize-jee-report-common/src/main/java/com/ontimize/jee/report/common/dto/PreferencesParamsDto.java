package com.ontimize.jee.report.common.dto;

import java.util.List;
import java.util.Objects;

public class PreferencesParamsDto {

	private String name;
	private String description;
	public boolean vertical;
	private String title;
	private String subtitle;
	private List<String> columns;
	private List<String> groups;
	private List<String> functions;
	private List<String> styleFunctions;
	private List<ColumnStyleParamsDto> columnsStyle;
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

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public List<ColumnStyleParamsDto> getColumnsStyle() {
		return columnsStyle;
	}

	public void setColumnsStyle(List<ColumnStyleParamsDto> columnsStyle) {
		this.columnsStyle = columnsStyle;
	}

	public PreferencesParamsDto() {
	}

	@Override
	public String toString() {
		return "PreferencesParamsDto [name=" + name + ", description=" + description + ", vertical=" + vertical
				+ ", title=" + title + ", subtitle=" + subtitle + ", columns=" + columns + ", groups=" + groups
				+ ", functions=" + functions + ", styleFunctions=" + styleFunctions + ", columnsStyle=" + columnsStyle
				+ ", entity=" + entity + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(columns, columnsStyle, description, entity, functions, groups, name, styleFunctions,
				subtitle, title, vertical);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PreferencesParamsDto other = (PreferencesParamsDto) obj;
		return Objects.equals(columns, other.columns) && Objects.equals(columnsStyle, other.columnsStyle)
				&& Objects.equals(description, other.description) && Objects.equals(entity, other.entity)
				&& Objects.equals(functions, other.functions) && Objects.equals(groups, other.groups)
				&& Objects.equals(name, other.name) && Objects.equals(styleFunctions, other.styleFunctions)
				&& Objects.equals(subtitle, other.subtitle) && Objects.equals(title, other.title)
				&& vertical == other.vertical;
	}

}
