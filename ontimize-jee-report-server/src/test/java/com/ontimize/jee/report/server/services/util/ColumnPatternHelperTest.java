package com.ontimize.jee.report.server.services.util;

import com.ontimize.jee.report.common.dto.renderer.CurrencyRendererDto;
import com.ontimize.jee.report.common.dto.renderer.IntegerRendererDto;
import com.ontimize.jee.report.common.dto.renderer.RealRendererDto;
import com.ontimize.jee.report.common.dto.renderer.RendererDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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

}
