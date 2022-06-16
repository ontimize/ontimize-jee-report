package com.ontimize.jee.report.common.dto;

import com.ontimize.jee.report.common.dto.renderer.RendererDto;

public class ColumnStyleParamsDto {

	Integer width;
	String alignment;
	
	RendererDto renderer;

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public String getAlignment() {
		return alignment;
	}

	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

	public RendererDto getRenderer() {
		return renderer;
	}

	public void setRenderer(RendererDto renderer) {
		this.renderer = renderer;
	}
}
