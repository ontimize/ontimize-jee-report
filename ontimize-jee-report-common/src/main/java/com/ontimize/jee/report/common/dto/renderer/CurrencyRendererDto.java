package com.ontimize.jee.report.common.dto.renderer;

public class CurrencyRendererDto extends RealRendererDto {
    
    private String currencySymbol;
    private String currencySymbolPosition;

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getCurrencySymbolPosition() {
        return currencySymbolPosition;
    }

    public void setCurrencySymbolPosition(String currencySymbolPosition) {
        this.currencySymbolPosition = currencySymbolPosition;
    }
}
