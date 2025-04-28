package com.ontimize.jee.report.common.dto.renderer;

public class RendererDto implements Renderer {

    private String type;

    private String format;

    @Override
    public String getType() {
        return this.type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
