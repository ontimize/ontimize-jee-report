package com.ontimize.jee.report.common.dto.renderer;

public class IntegerRendererDto extends RendererDto{
    
    private boolean grouping;
    private String thousandSeparator;

    public boolean isGrouping() {
        return grouping;
    }

    public void setGrouping(boolean grouping) {
        this.grouping = grouping;
    }

    public String getThousandSeparator() {
        return thousandSeparator;
    }

    public void setThousandSeparator(String thousandSeparator) {
        this.thousandSeparator = thousandSeparator;
    }
}
