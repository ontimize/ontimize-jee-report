package com.ontimize.jee.report.common.dto.renderer;

public class RendererDto implements Renderer{
    
    private String type;

    @Override
    public String getType() {
        return this.type;
    }
}
