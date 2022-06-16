package com.ontimize.jee.report.common.dto.renderer;

public class RealRendererDto extends IntegerRendererDto {
    
    private int decimalDigits = 2;

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }
}
