package com.ontimize.jee.common.services.reportstore;

import java.awt.Image;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ImageIcon;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.util.remote.BytesBlock;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class EntityResultDataSource implements JRDataSource {

	protected static EntityResult result;

    private int index = -1;

    private final int size;

    public EntityResultDataSource(EntityResult result) {
        this.result = result;

        this.size = result.calculateRecordNumber();
        this.index = -1;
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        Object obj = this.result.get(field.getName());
        if ((obj == null) || (!(obj instanceof Vector))) {
            return null;
        }
        Vector v = (Vector) obj;

        Class fieldClass = field.getValueClass();
        Object value = (this.index >= 0) && (this.index < this.size) ? v.get(this.index) : null;

        if (java.awt.Image.class.equals(fieldClass) && (value instanceof BytesBlock)) {
            Image im = new ImageIcon(((BytesBlock) value).getBytes()).getImage();
            v.setElementAt(im, this.index);
            value = im;
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

    public EntityResult getEntityResult() {
        return this.result;
    }

    public JRField[] getFields() {
        return EntityResultDataSource.getFields(this.result);
    }

    public static JRField[] getFields(EntityResult result) {
        Enumeration keys = result.keys();
        Vector tmp = new Vector();

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

                Hashtable m = new Hashtable();
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
