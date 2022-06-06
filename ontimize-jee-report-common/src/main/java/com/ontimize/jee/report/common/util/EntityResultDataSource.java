package com.ontimize.jee.report.common.util;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.ontimize.jee.common.dto.EntityResult;

import com.ontimize.jee.report.common.dto.ServiceRendererDto;
import com.ontimize.jee.report.common.reportstore.OntimizeField;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class EntityResultDataSource implements JRDataSource {

	protected static EntityResult result;
    
    protected Map<String, EntityResult> rendererData;
    
    protected Map<String, ServiceRendererDto> rendererInfo;

    public void setRendererData(Map<String, EntityResult> rendererData) {
        this.rendererData = rendererData;
    }

    public void setRendererInfo(Map<String, ServiceRendererDto> rendererInfo) {
        this.rendererInfo = rendererInfo;
    }

    private int index = -1;

    private final int size;

    @SuppressWarnings("static-access")
	public EntityResultDataSource(EntityResult result) {
        this.result = result;

        this.size = result.calculateRecordNumber();
        this.index = -1;
    }

    @SuppressWarnings({ "static-access", "rawtypes", "unchecked" })
	@Override
    public Object getFieldValue(JRField field) throws JRException {
        if(this.result == null) {
            return null;
        }
        String fieldName = field.getName();
        Class fieldClass = field.getValueClass();
        
        Object value = null;
        if ((this.index >= 0) && (this.index < this.size)) {
            Map recordValues = result.getRecordValues(this.index);
            if(recordValues == null){
                return null;
            }
            value = recordValues.get(fieldName);
        }

        if (java.awt.Image.class.equals(fieldClass)) {
            return getImageValue(value);
        } else if(rendererData.containsKey(fieldName)){
           return getServiceRendererValue(fieldName, value);
        }
        return value;
    }

    @Override
    public boolean next() throws JRException {
        this.index++;
        return this.index < this.size;
    }

    public void reset() {
        this.index = -1;
    }

    @SuppressWarnings("static-access")
	public EntityResult getEntityResult() {
        return this.result;
    }

    @SuppressWarnings("static-access")
	public JRField[] getFields() {
        return EntityResultDataSource.getFields(this.result);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static JRField[] getFields(EntityResult result) {
        Enumeration keys = result.keys();
        List tmp = new ArrayList();

        try {
            while (keys.hasMoreElements()) {
                Object o = keys.nextElement();
                if ((o == null) || (!(o instanceof String))) {
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
            if ((o == null) || (!(o instanceof OntimizeField))) {
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
    
    protected Object getServiceRendererValue(final String fieldName, final Object value) {
        EntityResult obj2 = rendererData.get(fieldName);
        if (obj2 == null) {
            return null;
        }
        if(rendererInfo == null || rendererInfo.get(fieldName) == null){
            return null;
        }
        Object obj3 = obj2.get(fieldName);
        if (!(obj3 instanceof List)) {
            return null;
        }
        List<?> v2 = (List<?>) obj3;
        int i = v2.indexOf(value);
        if(i >= 0 && i < v2.size()) {
            String valueColumn = this.rendererInfo.get(fieldName).getValueColumn();
            Map recordValues = obj2.getRecordValues(i);
            return recordValues.get(valueColumn);
        }
        return null;
    }
}
