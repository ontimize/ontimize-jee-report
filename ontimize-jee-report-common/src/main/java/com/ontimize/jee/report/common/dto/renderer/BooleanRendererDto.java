package com.ontimize.jee.report.common.dto.renderer;

public class BooleanRendererDto extends RendererDto {
    
    public static final String STRING_TYPE = "string";
    public static final String NUMBER_TYPE = "number";

    private String renderType; // string | number
    private String trueValue;
    private String falseValue;
    
    public String getRenderType() {
        return renderType;
    }

    public void setRenderType(String renderType) {
        this.renderType = renderType;
    }

    public String getTrueValue() {
        return trueValue;
    }

    public void setTrueValue(String trueValue) {
        this.trueValue = trueValue;
    }

    public String getFalseValue() {
        return falseValue;
    }

    public void setFalseValue(String falseValue) {
        this.falseValue = falseValue;
    }

}
