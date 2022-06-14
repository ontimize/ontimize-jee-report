package com.ontimize.jee.report.common.dto;

public class StyleParamsDto {
	private boolean grid;
	private boolean rowNumber;
	private boolean columnName;
	private boolean backgroundOnOddRows;
	private boolean hideGroupDetails;
	private boolean groupNewPage;
	private boolean firstGroupNewPage;

	public boolean isGrid() {
		return grid;
	}

	public void setGrid(boolean grid) {
		this.grid = grid;
	}

	public boolean isRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(boolean rowNumber) {
		this.rowNumber = rowNumber;
	}

	public boolean isColumnName() {
		return columnName;
	}

	public void setColumnName(boolean columnName) {
		this.columnName = columnName;
	}

	public boolean isBackgroundOnOddRows() {
		return backgroundOnOddRows;
	}

	public void setBackgroundOnOddRows(boolean backgroundOnOddRows) {
		this.backgroundOnOddRows = backgroundOnOddRows;
	}

	public boolean isHideGroupDetails() {
		return hideGroupDetails;
	}

	public void setHideGroupDetails(boolean hideGroupDetails) {
		this.hideGroupDetails = hideGroupDetails;
	}

	public boolean isGroupNewPage() {
		return groupNewPage;
	}

	public void setGroupNewPage(boolean groupNewPage) {
		this.groupNewPage = groupNewPage;
	}

	public boolean isFirstGroupNewPage() {
		return firstGroupNewPage;
	}

	public void setFirstGroupNewPage(boolean firstGroupNewPage) {
		this.firstGroupNewPage = firstGroupNewPage;
	}

}
