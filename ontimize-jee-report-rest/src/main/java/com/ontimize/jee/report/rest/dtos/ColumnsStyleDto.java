package com.ontimize.jee.report.rest.dtos;

public class ColumnsStyleDto {

	private String id;
	private String name;
	private int width;
	private String alignment;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getAlignment() {
		return alignment;
	}

	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

	public ColumnsStyleDto() {
	}

	@Override
	public String toString() {
		return "[id=" + id + ", name=" + name + ", width=" + width + ", alignment=" + alignment + "]";
	}

}
