package com.ontimize.jee.report.server.services.util;

import com.ontimize.jee.report.common.dto.renderer.CurrencyRendererDto;
import com.ontimize.jee.report.common.dto.renderer.IntegerRendererDto;
import com.ontimize.jee.report.common.dto.renderer.RealRendererDto;
import com.ontimize.jee.report.common.dto.renderer.RendererDto;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Date;

public class ColumnPatternHelper {

    private static final String defaultIntegerPattern = "0";
    private static final String defaultRealPattern = "#,##0.00";
    public static String getPatternForClass(Class<?> type, final RendererDto rendererDto) {
        String pattern = null;
        if (Long.class.isAssignableFrom(type)
                || Integer.class.isAssignableFrom(type)) {
            IntegerRendererDto renderer = rendererDto instanceof IntegerRendererDto ? (IntegerRendererDto) rendererDto : null;
            pattern = integerPattern(renderer);
        } else if (Number.class.isAssignableFrom(type)) {
            if(rendererDto instanceof CurrencyRendererDto) {
                pattern = currencyPattern((CurrencyRendererDto) rendererDto);
            } else if(rendererDto instanceof RealRendererDto) {
                pattern = realPattern((RealRendererDto) rendererDto);
            } else {
                pattern = defaultRealPattern;
            }
        } else if (Date.class.isAssignableFrom(type)
                || LocalDate.class.isAssignableFrom(type)) {
            pattern = "dd/MM/yyyy";
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            pattern = "dd/MM/yyyy HH:mm";
        } else if (YearMonth.class.isAssignableFrom(type)) {
            pattern = "MM/yyyy";
        }
        
        return pattern;
    }
    
    static String integerPattern(final IntegerRendererDto rendererDto) {
        String pattern = defaultIntegerPattern;
        if(rendererDto != null) {
            if(rendererDto.isGrouping()){
                pattern = "#,##0";
            }
        }
        return pattern;
    }

    static String realPattern(final RealRendererDto rendererDto) {
        String pattern = defaultRealPattern;
        if(rendererDto != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(rendererDto.isGrouping() ? "#,##0" : "0");
            if(rendererDto.getDecimalDigits() > 0) {
                sb.append(".")
                    .append(StringUtils.rightPad("", rendererDto.getDecimalDigits(), "0"));
            }
            pattern = sb.toString();
        }
        return pattern;
    }
    
    static String currencyPattern(final CurrencyRendererDto rendererDto) {
        String pattern = defaultRealPattern;
        if(rendererDto != null) {
            StringBuilder sb = new StringBuilder();
            if("left".equals(rendererDto.getCurrencySymbolPosition() )){
                sb.append(rendererDto.getCurrencySymbol());
            }
            sb.append(rendererDto.isGrouping() ? "#,##0" : "0");
            if(rendererDto.getDecimalDigits() > 0) {
                sb.append(".")
                        .append(StringUtils.rightPad("", rendererDto.getDecimalDigits(), "0"));
            }
            if("right".equals(rendererDto.getCurrencySymbolPosition() )){
                sb.append(rendererDto.getCurrencySymbol());
            }
            pattern = sb.toString();
        }
        return pattern;
    }
}
