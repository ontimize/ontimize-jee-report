package com.ontimize.jee.report.server.services.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MomentJSDateUtil {

    private static MomentJSDateUtil instance;

    private static final List<String> momentFormats = Arrays.asList("LT", "LTS", "L", "l", "LL", "ll", "LLL", "lll", "LLLL", "llll");

    private final Map<String, Map<String, String>> cache = new HashMap<>();

    private MomentJSDateUtil() {
        initializeCache();
    }

    public static MomentJSDateUtil getInstance() {
        if (instance == null) {
            instance = new MomentJSDateUtil();
        }
        return instance;
    }

    public String getPatternFromMommentJsFormat(final String format, final Locale locale) {
        if (StringUtils.isBlank(format) || !momentFormats.contains(format)) {
            return format;
        }

        String lang = "en";
        if (locale != null) {
            lang = locale.getLanguage();
        }
        Map<String, String> longDateFormatMap = this.cache.get(lang);
        if (longDateFormatMap != null) {
            return longDateFormatMap.get(format);
        }
        return null;
    }

    protected void initializeCache() {

        cache.put("en", getENLongDateFormat());
        cache.put("es", getESLongDateFormat());
    }

    protected Map<String, String> getESLongDateFormat() {
        Map<String, String> map = new HashMap<>();
        map.put("LT", "H:mm");
        map.put("LTS", "H:mm:ss");
        map.put("L", "dd/MM/YYYY");
        map.put("l", "d/M/YYYY");
        map.put("LL", "d 'de' MMMM 'de' YYYY");
        map.put("ll", "d 'de' MMM 'de' YYYY");
        map.put("LLL", "d 'de' MMMM 'de' YYYY H:mm");
        map.put("lll", "d 'de' MMM 'de' YYYY H:mm");
        map.put("LLLL", "EEEE, d 'de' MMMM 'de' YYYY H:mm");
        map.put("llll", "EE, d 'de' MMM 'de' YYYY H:mm");
        return map;
    }

    protected Map<String, String> getENLongDateFormat() {
        Map<String, String> map = new HashMap<>();
        map.put("LT", "h:mm a");
        map.put("LTS", "h:mm:ss a");
        map.put("L", "MM/dd/YYYY");
        map.put("l", "M/d/YYYY");
        map.put("LL", "MMMM d, YYYY");
        map.put("ll", "MMM d, YYYY");
        map.put("LLL", "MMMM d, YYYY h:mm a");
        map.put("lll", "MMM d, YYYY h:mm a");
        map.put("LLLL", "EEEE, MMMM d, YYYY h:mm a");
        map.put("llll", "EE, MMM d, YYYY h:mm a");
        return map;
    }
}
