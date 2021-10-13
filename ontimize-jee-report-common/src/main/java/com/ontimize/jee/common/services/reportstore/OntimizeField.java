package com.ontimize.jee.common.services.reportstore;

import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.JRPropertyExpression;

public class OntimizeField implements JRField {

	public static final String NAME_KEY = "NAME",
            DESCRIPTION_KEY = "DESCRIPTION",
            VALUE_CLASS_KEY = "VALUE_CLASS",
            VALUE_CLASS_NAME_KEY = "VALUE_CLASS_KEY",
            PROPERTIES_MAP_KEY = "PROPERTIES_MAP";

    private static final String MSG_NAME_NULL = "Parameter name must be exits";

//    private static final String MSG_DESCRIPTION_DEFAULT = "OntimizeField -> Default description.";
//
//    private static final String MSG_CLASS_DEFAULT = "OntimizeField -> Default class.";
//
//    private static final String MSG_CLASS_NAME_DEFAULT = "OntimizeField -> Default class name.";
//
//    private static final String MSG_PROPERTIES_DEFAULT = "OntimizeField -> Default properties map.";

    protected String name;

    protected String description;

    @SuppressWarnings("rawtypes")
	protected Class valueClass;

    protected String valueClassName;

    protected JRPropertiesMap propertiesMap;

    @SuppressWarnings("rawtypes")
	public OntimizeField(java.util.Map m) throws IllegalArgumentException {
        Object o = m.get(NAME_KEY);
        if (o == null || o instanceof String == false) {
            throw new IllegalArgumentException(MSG_NAME_NULL);
        }
        name = (String) o;

        o = m.get(DESCRIPTION_KEY);
        if ((o != null) && (o instanceof String)) {
            description = (String) o;
        } else {
            description = new String();
        }

        o = m.get(VALUE_CLASS_KEY);
        if ((o != null) && (o instanceof Class)) {
            valueClass = (Class) o;
        } else {
            valueClass = Object.class;
        }

        o = m.get(VALUE_CLASS_NAME_KEY);
        if ((o != null) && (o instanceof String)) {
            valueClassName = (String) o;
        } else {
            valueClassName = new String();
        }

        o = m.get(PROPERTIES_MAP_KEY);
        if ((o != null) && (o instanceof JRPropertiesMap)) {
            propertiesMap = (JRPropertiesMap) o;
        } else {
            propertiesMap = new JRPropertiesMap();
        }
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getValueClass() {
        return valueClass;
    }

    public String getValueClassName() {
        return valueClassName;
    }

    public JRPropertiesHolder getParentProperties() {
        return null;
    }

    public boolean hasProperties() {
        return false;
    }

    public JRPropertiesMap getPropertiesMap() {
        return propertiesMap;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append(", ");
        sb.append(description);
        sb.append(", ");
        sb.append(valueClassName);
        return sb.toString();
    }

    public Object clone() {
        return this;
    }

	@Override
	public JRPropertyExpression[] getPropertyExpressions() {
		// TODO Auto-generated method stub
		return null;
	}

}
