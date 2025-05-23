package com.ontimize.jee.report.common.util;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TypeMappingsUtils {

    public static final String STRING = "String",
            BOOLEAN = "Boolean",
            BINARY = "Binary",
            DOUBLE = "Double",
            DATE = "Date",
            INTEGER = "Integer",
            BIGDECIMAL = "BigDecimal",

    STRING_PATH = "java.lang.String",
            BOOLEAN_PATH = "java.lang.Boolean",
            OBJECT_PATH = "java.lang.Object",
            FLOAT_PATH = "java.lang.Float",
            DOUBLE_PATH = "java.lang.Double",
            DATE_PATH = "java.util.Date",
            TIMESTAMP_PATH = "ava.sql.Timestamp",
            INTEGER_PATH = "java.lang.Integer",
            BIGDECIMAL_PATH = "java.math.BigDecimal";

    @SuppressWarnings("rawtypes")
    public static Class getClass(int type) {
        switch (type) {
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
                return String.class;

            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.BIGINT:
                return Integer.class;

            case Types.BIT:
            case Types.BOOLEAN:
                return Boolean.class;

            case Types.DOUBLE:
            case Types.DECIMAL:
            case Types.REAL:
            case Types.FLOAT:
                return Double.class;

            case Types.NUMERIC:
                return BigDecimal.class;

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return Date.class;

            case Types.BINARY:
            case Types.LONGVARBINARY:
            case Types.VARBINARY:
            case Types.ARRAY:
            case Types.BLOB:
            case Types.OTHER:
                return Object.class;
        }
        return Object.class;
    }

    @SuppressWarnings("rawtypes")
    public static Class getClass(String type) {
        return getClass(getSQLType(type));
    }

    public static String getClassName(int type) {
        switch (type) {
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
                return STRING_PATH;

            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.BIGINT:
                return INTEGER_PATH;

            case Types.BIT:
            case Types.BOOLEAN:
                return BOOLEAN_PATH;

            case Types.DOUBLE:
            case Types.DECIMAL:
            case Types.REAL:
            case Types.FLOAT:
                return DOUBLE_PATH;

            case Types.NUMERIC:
                return BIGDECIMAL_PATH;

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return DATE_PATH;

            case Types.BINARY:
            case Types.LONGVARBINARY:
            case Types.VARBINARY:
            case Types.ARRAY:
            case Types.BLOB:
            case Types.OTHER:
                return OBJECT_PATH;
        }
        return OBJECT_PATH;
    }

    public static String getClassName(String type) {
        return getClassName(getSQLType(type));
    }

    public static int getSQLTypeFromClassName(final String className) {
        switch (className) {
            case STRING_PATH:
                return Types.VARCHAR;

            case INTEGER_PATH:
                return Types.INTEGER;

            case BOOLEAN_PATH:
                return Types.BOOLEAN;

            case FLOAT_PATH:
                return Types.FLOAT;

            case DOUBLE_PATH:
                return Types.DOUBLE;

            case BIGDECIMAL_PATH:
                return Types.NUMERIC;

            case DATE_PATH:
                return Types.DATE;

            case TIMESTAMP_PATH:
                return Types.TIMESTAMP;

        }
        return Types.OTHER;
    }

    public static int getSQLType(String type) {
        int returned = Types.OTHER;
        if (type.equalsIgnoreCase(STRING)) {
            returned = Types.VARCHAR;
        } else if (type.equalsIgnoreCase(BOOLEAN)) {
            returned = Types.BOOLEAN;
        } else if (type.equalsIgnoreCase(BINARY)) {
            returned = Types.BINARY;
        } else if (type.equalsIgnoreCase(DOUBLE)) {
            returned = Types.DOUBLE;
        } else if (type.equalsIgnoreCase(BIGDECIMAL)) {
            returned = Types.NUMERIC;
        }else if (type.equalsIgnoreCase(DATE)) {
            returned = Types.DATE;
        } else if (type.equalsIgnoreCase(INTEGER)) {
            returned = Types.INTEGER;
        }

        // Returns java.sql.Types.OTHER
        return returned;
    }

    /**
     * Transforms current Map with SQL types as strings in a new Map with SQL types as integers.
     */
    @SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
    public static Map convertStrSQLMap2IntSQLMap(Map m) {
        HashMap newMap = new HashMap(m.size());

        Set k = m.keySet();
        Collection v = m.values();

        Iterator ik = k.iterator();
        Iterator iv = v.iterator();

        while (ik.hasNext() && iv.hasNext()) {
            Object o = ik.next();
            if (!(o instanceof String)) {
                iv.next();
                continue;
            }
            String key = (String) o;

            o = iv.next();
            if (!(o instanceof String)) {
                continue;
            }
            String value = (String) o;

            newMap.put(key, new Integer(getSQLType(value)));
        }
        return newMap;
    }
}
