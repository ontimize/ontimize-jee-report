package com.ontimize.jee.common.services.reportstore;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.ontimize.jee.common.db.AdvancedEntityResult;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.common.util.remote.BytesBlock;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class AdvancedEntityResultDataSource implements JRDataSource  {

	protected static AdvancedEntityResult result;

    private int index = -1;

    private final int size;
    
    private final int totalSize;
    
    private int pagesize, offset;
    
    private Object bean;
    private String method;
    private Map<String, Object> reportParameters;
    private List<Object> attributes;
    private List<?> order;

    @SuppressWarnings("static-access")
	public AdvancedEntityResultDataSource(Object bean, String method, Map<String, Object> reportParameters,
    		List<Object> attributes, int pagesize, int offset, List<?> order) {
    	this.bean = bean;
    	this.method = method;
    	this.reportParameters = reportParameters;
    	this.attributes = attributes;
    	this.pagesize = pagesize;
    	this.offset = offset;
    	this.order = order;
    	
        this.result = (AdvancedEntityResult) ReflectionTools.invoke(bean, method, reportParameters, 
        		attributes, pagesize, offset, order);
        
        this.totalSize = this.result.getTotalRecordCount();
        this.size = result.calculateRecordNumber();
        this.index = -1;
    }

    @SuppressWarnings({ "rawtypes", "static-access", "unchecked" })
	@Override
    public Object getFieldValue(JRField field) throws JRException {
		Object obj = this.result.get(field.getName());
        if ((obj == null) || (!(obj instanceof List))) {
            return null;
        }
//        Vector v = (Vector) obj;
        List v = (ArrayList) obj;

        Class fieldClass = field.getValueClass();
        int internalIndex = this.index - this.offset;
        Object value = (internalIndex >= 0) && (internalIndex < this.size) ? v.get(internalIndex) : null;

//        if (java.awt.Image.class.equals(fieldClass) && (value instanceof BytesBlock)) {
//            Image im = new ImageIcon(((BytesBlock) value).getBytes()).getImage();
////            v.setElementAt(im, internalIndex);
//            v.set(this.index, im);
//            value = im;
//        }
        if (java.awt.Image.class.equals(fieldClass)) {
        	if (value instanceof BytesBlock) {
        		Image im = new ImageIcon(((BytesBlock) value).getBytes()).getImage();
//              v.setElementAt(im, this.index);
	            v.set(this.index, im);
	            value = im;
        	} else if (value instanceof String) {
        		Image im = new ImageIcon(Base64.getDecoder().decode((String) value)).getImage();
//              v.setElementAt(im, this.index);
        		v.set(this.index, im);
        		value = im;
        	}
        }
        
        return value;
    }

    @SuppressWarnings("static-access")
	@Override
    public boolean next() throws JRException {
        this.index++;
        if (this.index >= this.totalSize) {
        	return false;
        }
        if (this.index >= this.size + this.offset) {
        	this.offset += pagesize;
        	this.result = (AdvancedEntityResult) ReflectionTools.invoke(bean, method, reportParameters, 
            		attributes, pagesize, offset, order);
        }
        return true;
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
//        Vector tmp = new Vector();
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

//                Hashtable m = new Hashtable();
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
}
