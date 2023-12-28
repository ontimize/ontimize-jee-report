package com.ontimize.jee.report.server.services.util;

import com.ontimize.jee.report.common.dto.renderer.*;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

public class ColumnPatternHelper {

    private static final String defaultIntegerPattern = "0";
    private static final String defaultRealPattern = "#,##0.00";

    private static final String defaultDatePattern = "dd/MM/yyyy";

    private static final String defaultDateTimePattern = "dd/MM/yyyy HH:mm";

    public static String getPatternForClass(Class<?> type, final RendererDto rendererDto) {
        return getPatternForClass(type, rendererDto, Locale.getDefault());
    }

    public static String getPatternForClass(Class<?> type, final RendererDto rendererDto, final Locale locale) {
        String pattern = null;
        if (Long.class.isAssignableFrom(type)
                || Integer.class.isAssignableFrom(type)) {
            IntegerRendererDto renderer = rendererDto instanceof IntegerRendererDto ? (IntegerRendererDto) rendererDto : null;
            pattern = integerPattern(renderer);
        } else if (Number.class.isAssignableFrom(type)) {
            if (rendererDto instanceof CurrencyRendererDto) {
                pattern = currencyPattern((CurrencyRendererDto) rendererDto);
            } else if (rendererDto instanceof RealRendererDto) {
                pattern = realPattern((RealRendererDto) rendererDto);
            } else {
                pattern = defaultRealPattern;
            }
        } else if (Date.class.isAssignableFrom(type)
                || LocalDate.class.isAssignableFrom(type)) {
            DateRendererDto renderer = rendererDto instanceof DateRendererDto ? (DateRendererDto) rendererDto : null;
            pattern = datePattern(renderer, locale);
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            DateRendererDto renderer = rendererDto instanceof DateRendererDto ? (DateRendererDto) rendererDto : null;
            pattern = dateTimePattern(renderer, locale);
        } 

        return pattern;
    }

    static String integerPattern(final IntegerRendererDto rendererDto) {
        String pattern = defaultIntegerPattern;
        if (rendererDto != null) {
            if (rendererDto.isGrouping()) {
                pattern = "#,##0";
            }
        }
        return pattern;
    }

    static String realPattern(final RealRendererDto rendererDto) {
        String pattern = defaultRealPattern;
        if (rendererDto != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(rendererDto.isGrouping() ? "#,##0" : "0");
            if (rendererDto.getDecimalDigits() > 0) {
                sb.append(".")
                        .append(StringUtils.rightPad("", rendererDto.getDecimalDigits(), "0"));
            }
            pattern = sb.toString();
        }
        return pattern;
    }

    static String currencyPattern(final CurrencyRendererDto rendererDto) {
        String pattern = defaultRealPattern;
        if (rendererDto != null) {
            StringBuilder sb = new StringBuilder();
            if ("left".equals(rendererDto.getCurrencySymbolPosition())) {
                sb.append(rendererDto.getCurrencySymbol());
            }
            sb.append(rendererDto.isGrouping() ? "#,##0" : "0");
            if (rendererDto.getDecimalDigits() > 0) {
                sb.append(".")
                        .append(StringUtils.rightPad("", rendererDto.getDecimalDigits(), "0"));
            }
            if ("right".equals(rendererDto.getCurrencySymbolPosition())) {
                sb.append(rendererDto.getCurrencySymbol());
            }
            pattern = sb.toString();
        }
        return pattern;
    }

    static String datePattern(final DateRendererDto rendererDto, Locale locale) {
        String pattern = defaultDatePattern;
        if (locale != null) {
            DateFormat formatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
            pattern = ((SimpleDateFormat) formatter).toLocalizedPattern();
        }

        if (rendererDto != null && !StringUtils.isBlank(rendererDto.getFormat())) {
            String pattern1 = MomentJSDateUtil.getInstance().getPatternFromMommentJsFormat(rendererDto.getFormat(), locale);
            if (!StringUtils.isBlank(pattern1)) {
                pattern = pattern1;
            }
        }
        return pattern;
    }

    static String dateTimePattern(final DateRendererDto rendererDto, Locale locale) {
        String pattern = defaultDateTimePattern;
        if (locale != null) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, locale);
            pattern = ((SimpleDateFormat) formatter).toLocalizedPattern();
        }
        if (rendererDto != null && !StringUtils.isBlank(rendererDto.getFormat())) {
            String pattern1 = MomentJSDateUtil.getInstance().getPatternFromMommentJsFormat(rendererDto.getFormat(), locale);
            if (!StringUtils.isBlank(pattern1)) {
                pattern = pattern1;
            }
        }
        return pattern;
    }
}
