package com.ontimize.jee.report.server.services.util;

import com.ontimize.jee.report.common.dto.renderer.CurrencyRendererDto;
import com.ontimize.jee.report.common.dto.renderer.DateRendererDto;
import com.ontimize.jee.report.common.dto.renderer.IntegerRendererDto;
import com.ontimize.jee.report.common.dto.renderer.RealRendererDto;
import com.ontimize.jee.report.common.dto.renderer.RendererDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ColumnPatternHelperTest {

    @Test
    public void testIntegerPatternNoGrouping() {
        IntegerRendererDto renderer = new IntegerRendererDto();
        renderer.setGrouping(false);
        String pattern = ColumnPatternHelper.getPatternForClass(Integer.class, renderer);
        
        Assertions.assertNotNull(pattern);

        DecimalFormat format = new DecimalFormat(pattern);
        Assertions.assertEquals("2000",  format.format(2000));
    }

    @Test
    public void testIntegerPatternGrouping() {
        IntegerRendererDto renderer = new IntegerRendererDto();
        renderer.setGrouping(true);
        String pattern = ColumnPatternHelper.getPatternForClass(Integer.class, renderer);

        Assertions.assertNotNull(pattern);

        DecimalFormat format = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.ENGLISH));
        Assertions.assertEquals("2,000", format.format(2000));


        Locale customLocale = new Locale("es", "ES");
        format = new DecimalFormat(pattern, new DecimalFormatSymbols(customLocale));
        Assertions.assertEquals("2.000", format.format(2000));
    }

    @Test
    public void testRealPatternNoGrouping() {
        RealRendererDto renderer = new RealRendererDto();
        renderer.setGrouping(false);
        String pattern = ColumnPatternHelper.getPatternForClass(Double.class, renderer);

        Assertions.assertNotNull(pattern);

        DecimalFormat format = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.ENGLISH));
        Assertions.assertEquals("5000.00", format.format(5000));
    }

    @Test
    public void testRealPatternGrouping() {
        RealRendererDto renderer = new RealRendererDto();
        renderer.setGrouping(true);
        String pattern = ColumnPatternHelper.getPatternForClass(Double.class, renderer);

        Assertions.assertNotNull(pattern);

        DecimalFormat format = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.ENGLISH));
        Assertions.assertEquals("2,000.00",  format.format(2000));


        Locale customLocale = new Locale("es", "ES");
        format = new DecimalFormat(pattern, new DecimalFormatSymbols(customLocale));
        Assertions.assertEquals("2.000,00", format.format(2000));
    }

    @Test
    public void testRealPatternNoDecimals() {
        RealRendererDto renderer = new RealRendererDto();
        renderer.setGrouping(true);
        renderer.setDecimalDigits(0);
        String pattern = ColumnPatternHelper.getPatternForClass(Double.class, renderer);

        Assertions.assertNotNull(pattern);

        DecimalFormat format = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.ENGLISH));
        Assertions.assertEquals("2,000", format.format(2000));


        Locale customLocale = new Locale("es", "ES");
        format = new DecimalFormat(pattern, new DecimalFormatSymbols(customLocale));
        Assertions.assertEquals("2.000", format.format(2000));
    }

    @Test
    public void testRealPatternCustomDecimals() {
        RealRendererDto renderer = new RealRendererDto();
        renderer.setGrouping(true);
        renderer.setDecimalDigits(4);
        String pattern = ColumnPatternHelper.getPatternForClass(Double.class, renderer);

        Assertions.assertNotNull(pattern);

        DecimalFormat format = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.ENGLISH));
        Assertions.assertEquals("2,000.2300", format.format(2000.23));
        Assertions.assertEquals("2,000.1234", format.format(2000.1234));
        Assertions.assertEquals("2,000.1235", format.format(2000.12349));


    }

    @Test
    public void testCurrencyPatternCustomDecimalsAndSymbolFirst() {
        CurrencyRendererDto renderer = new CurrencyRendererDto();
        renderer.setGrouping(true);
        renderer.setDecimalDigits(4);
        renderer.setCurrencySymbol("$");
        renderer.setCurrencySymbolPosition("left");
        String pattern = ColumnPatternHelper.getPatternForClass(Double.class, renderer);

        Assertions.assertNotNull(pattern);

        DecimalFormat format = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.ENGLISH));
        Assertions.assertEquals("$2,000.2300", format.format(2000.23));
        Assertions.assertEquals("$2,000.1234", format.format(2000.1234));
        Assertions.assertEquals("$2,000.1235", format.format(2000.12349));
    }

    @Test
    public void testCurrencyPatternCustomDecimalsAndSymbolEnd() {
        CurrencyRendererDto renderer = new CurrencyRendererDto();
        renderer.setGrouping(true);
        renderer.setDecimalDigits(4);
        renderer.setCurrencySymbol("€");
        renderer.setCurrencySymbolPosition("right");
        String pattern = ColumnPatternHelper.getPatternForClass(Double.class, renderer);

        Assertions.assertNotNull(pattern);

        DecimalFormat format = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.ENGLISH));
        Assertions.assertEquals("2,000.2300€", format.format(2000.23));
        Assertions.assertEquals("2,000.1234€", format.format(2000.1234));
        Assertions.assertEquals("2,000.1235€", format.format(2000.12349));
    }


    @Test
    public void testDefaultDatePattern() {
        DateRendererDto renderer = new DateRendererDto();
        //2022/06/28 13:24:56
        Calendar calendar = new GregorianCalendar(2022,5,28,13,24,56);
        Date targetDate = calendar.getTime();
        Locale es_locale = new Locale("es", "ES");

        DateFormat format = getDateFormat(Date.class, renderer, Locale.ENGLISH);
        Assertions.assertEquals("Jun 28, 2022", format.format(targetDate));
        format = getDateFormat(Date.class, renderer, es_locale);
        Assertions.assertEquals("28 jun. 2022", format.format(targetDate));
    }
    
    @Test
    public void testDatePatternUsingMomentJSFormat() {
        DateRendererDto renderer = new DateRendererDto();
        //2022/06/28 13:24:56
        Calendar calendar = new GregorianCalendar(2022,5,28,13,24,56);
        Date targetDate = calendar.getTime();
        Locale es_locale = new Locale("es", "ES");

        // Case 'L'
        renderer.setFormat("L");
        DateFormat format = getDateFormat(Date.class, renderer, Locale.ENGLISH);
        Assertions.assertEquals("06/28/2022", format.format(targetDate));
        format = getDateFormat(Date.class, renderer, es_locale);
        Assertions.assertEquals("28/06/2022", format.format(targetDate));

        // Case 'LL'
        renderer.setFormat("LL");
        format = getDateFormat(Date.class, renderer, Locale.ENGLISH);
        Assertions.assertEquals("June 28, 2022", format.format(targetDate));
        format = getDateFormat(Date.class, renderer, es_locale);
        Assertions.assertEquals("28 de junio de 2022", format.format(targetDate));

        // Case 'LLL'
        renderer.setFormat("LLL");
        format = getDateFormat(Date.class, renderer, Locale.ENGLISH);
        Assertions.assertEquals("June 28, 2022 1:24 PM", format.format(targetDate));
        format = getDateFormat(Date.class, renderer, es_locale);
        Assertions.assertEquals("28 de junio de 2022 13:24", format.format(targetDate));

        // Case 'LLLL'
        renderer.setFormat("LLLL");
        format = getDateFormat(Date.class, renderer, Locale.ENGLISH);
        Assertions.assertEquals("Tuesday, June 28, 2022 1:24 PM", format.format(targetDate));
        format = getDateFormat(Date.class, renderer, es_locale);
        Assertions.assertEquals("martes, 28 de junio de 2022 13:24", format.format(targetDate));


        // Case 'l'
        renderer.setFormat("l");
        format = getDateFormat(Date.class, renderer, Locale.ENGLISH);
        Assertions.assertEquals("6/28/2022", format.format(targetDate));
        format = getDateFormat(Date.class, renderer, es_locale);
        Assertions.assertEquals("28/6/2022", format.format(targetDate));
        
        // Case 'll'
        renderer.setFormat("ll");
        format = getDateFormat(Date.class, renderer, Locale.ENGLISH);
        Assertions.assertEquals("Jun 28, 2022", format.format(targetDate));
        format = getDateFormat(Date.class, renderer, es_locale);
        Assertions.assertEquals("28 de jun. de 2022", format.format(targetDate));

        // Case 'lll'
        renderer.setFormat("lll");
        format = getDateFormat(Date.class, renderer, Locale.ENGLISH);
        Assertions.assertEquals("Jun 28, 2022 1:24 PM", format.format(targetDate));
        format = getDateFormat(Date.class, renderer, es_locale);
        Assertions.assertEquals("28 de jun. de 2022 13:24", format.format(targetDate));

        // Case 'llll'
        renderer.setFormat("llll");
        format = getDateFormat(Date.class, renderer, Locale.ENGLISH);
        Assertions.assertEquals("Tue, Jun 28, 2022 1:24 PM", format.format(targetDate));
        format = getDateFormat(Date.class, renderer, es_locale);
        Assertions.assertEquals("mar., 28 de jun. de 2022 13:24", format.format(targetDate));
    }
    
    private DateFormat getDateFormat(Class<?> dateClass, DateRendererDto renderer, Locale locale) {
        String pattern = ColumnPatternHelper.getPatternForClass(dateClass, renderer, locale);
        Assertions.assertNotNull(pattern);
        return new SimpleDateFormat(pattern,locale);
    }

    @Test
    public void testDefaultDateTimePattern() {
        DateRendererDto renderer = new DateRendererDto();
        //2022/06/28 13:24:56
        Calendar calendar = new GregorianCalendar(2022,5,28,13,24,56);
        Date targetDate = calendar.getTime();
        Locale es_locale = new Locale("es", "ES");

        DateFormat format = getDateFormat(LocalDateTime.class, renderer, Locale.ENGLISH);
        Assertions.assertEquals("Jun 28, 2022, 1:24 PM", format.format(targetDate));
        format = getDateFormat(LocalDateTime.class, renderer, es_locale);
        Assertions.assertEquals("28 jun. 2022 13:24", format.format(targetDate));
    }

}
