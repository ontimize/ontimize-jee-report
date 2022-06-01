package com.ontimize.jee.report.common.dto;

public class OrderByDto {
	private String columnId = null;
	private String columnName = null;
	private boolean ascendent = true;

	public OrderByDto() {
		// no-op
	}

	public OrderByDto(String columnId, String columnName, boolean ascendent) {
		this.columnId = columnId;
		this.columnName = columnName;
		this.ascendent = ascendent;
	}

	public String getColumnId() {
		return columnId;
	}

	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean isAscendent() {
		return ascendent;
	}

	public void setAscendent(boolean ascendent) {
		this.ascendent = ascendent;
	}

}
