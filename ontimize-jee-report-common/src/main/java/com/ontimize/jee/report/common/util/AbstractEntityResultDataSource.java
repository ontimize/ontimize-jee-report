package com.ontimize.jee.report.common.util;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.report.common.dto.renderer.BooleanRendererDto;
import com.ontimize.jee.report.common.dto.renderer.Renderer;
import com.ontimize.jee.report.common.dto.renderer.ServiceRendererDto;
import com.ontimize.jee.report.common.reportstore.OntimizeField;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractEntityResultDataSource<T extends EntityResult> implements JRDataSource {

    protected T result;
    protected Map<String, T> rendererData;
    protected Map<String, Renderer> rendererInfo;
    protected int index = -1;
    protected final int size;

    public AbstractEntityResultDataSource(T result) {
        this.result = result;

        this.size = result.calculateRecordNumber();
        this.index = -1;
        this.rendererData = new HashMap<>();
        this.rendererInfo = new HashMap<>();
    }

    public void setRendererData(Map<String, T> rendererData) {
        this.rendererData = rendererData;
    }

    public void addRendererData(Map<String, T> rendererData) {
        if(rendererData != null && !rendererData.isEmpty()) {
            this.rendererData.putAll(rendererData);
        }
    }
    
    public void clearRendererData() {
        this.rendererData.clear();
    }

    public void setRendererInfo(Map<String, Renderer> rendererInfo) {
        this.rendererInfo = rendererInfo;
    }

    public void addRendererInfo(Map<String, Renderer> rendererInfo) {
        if(rendererInfo != null && !rendererInfo.isEmpty()) {
            this.rendererInfo.putAll(rendererInfo);
        }
    }

    public void clearRendererInfo() {
        this.rendererInfo.clear();
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        if (this.result == null) {
            return null;
        }
        String fieldName = field.getName();
        Class<?> fieldClass = field.getValueClass();

        Object value = null;
        int auxIdx = calculateIndex();
        if ((auxIdx >= 0) && (auxIdx < this.size)) {
            Map<?, ?> recordValues = getEntityResult().getRecordValues(this.index);
            if (recordValues == null) {
                return null;
            }
            value = recordValues.get(fieldName);
        }

        if (Image.class.equals(fieldClass)) {
            return getImageValue(value);
        } else if (rendererInfo != null && rendererInfo.containsKey(fieldName)) {
            return getRendererValue(fieldName, value);
        }
        return value;
    }

    public abstract int calculateIndex();

    @Override
    public boolean next() throws JRException {
        this.index++;
        return this.index < this.size;
    }

    public void reset() {
        this.index = -1;
    }

    public T getEntityResult() {
        return this.result;
    }

    public JRField[] getFields() {
        return AbstractEntityResultDataSource.getFields(this.result);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static JRField[] getFields(EntityResult result) {
        Enumeration keys = result.keys();
        List tmp = new ArrayList();

        try {
            while (keys.hasMoreElements()) {
                Object o = keys.nextElement();
                if ((!(o instanceof String))) {
                    continue;
                }

                String name = (String) o;
                int type = result.getColumnSQLType(name);
                Class classClass = TypeMappingsUtils.getClass(type);
                String className = TypeMappingsUtils.getClassName(type);

                Map m = new HashMap();
                m.put(OntimizeField.NAME_KEY, name);
                m.put(OntimizeField.VALUE_CLASS_NAME_KEY, className);
                m.put(OntimizeField.VALUE_CLASS_KEY, classClass);

                tmp.add(new OntimizeField(m));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // To array
        int s = tmp.size();
        OntimizeField[] a = new OntimizeField[s];
        for (int i = 0; i < s; i++) {
            Object o = tmp.get(i);
            if ((!(o instanceof OntimizeField))) {
                continue;
            }
            a[i] = (OntimizeField) o;
        }
        return a;
    }


    protected Object getImageValue(final Object value) {
        Object result_ = null;
        if (value instanceof byte[]) {
            result_ = new ImageIcon((byte[]) value).getImage();
        } else if (value instanceof String) {
            result_ = new ImageIcon(Base64.getDecoder().decode((String) value)).getImage();
        }
        return result_;
    }

    protected Object getRendererValue(final String fieldName, final Object value) {
        if (rendererInfo == null || rendererInfo.get(fieldName) == null) {
            return null;
        }
        
        Renderer renderer = this.rendererInfo.get(fieldName);
        if(renderer instanceof ServiceRendererDto) {
            return getServiceRendererValue(fieldName, value);
        } else if( renderer instanceof BooleanRendererDto) {
            return getBooleanRendererValue((BooleanRendererDto) renderer, value);
        }
        return null;
    }

    protected Object getServiceRendererValue(final String fieldName, final Object value) {
        EntityResult entityResult = rendererData.get(fieldName);
        if (entityResult == null) {
            return null;
        }
        Object obj3 = entityResult.get(fieldName);
        if (!(obj3 instanceof List)) {
            return null;
        }
        List<?> v2 = (List<?>) obj3;
        int i = v2.indexOf(value);
        if (i >= 0 && i < v2.size()) {
            ServiceRendererDto renderer = (ServiceRendererDto) this.rendererInfo.get(fieldName);
            String valueColumn = renderer.getValueColumn();
            Map<?, ?> recordValues = entityResult.getRecordValues(i);
            return recordValues.get(valueColumn);
        }
        return null;
    }

    protected Object getBooleanRendererValue(final BooleanRendererDto renderer, final Object value) {
        if(value == null) {
            return value;
        }
        if(value instanceof Boolean){
            if(value == Boolean.TRUE) {
                return renderer.getTrueValue();
            } else if(value == Boolean.FALSE) {
                return renderer.getFalseValue();
            }
        } else if(value instanceof Number) {
            if(((Number) value).intValue() == 1) {
                return renderer.getTrueValue();
            } else if(((Number) value).intValue() == 0) {
                return renderer.getFalseValue();
            }
        }
        return value;
    }
}
