package com.ontimize.jee.report.common.util;

import com.ontimize.jee.common.db.AdvancedEntityResult;
import com.ontimize.jee.common.tools.ReflectionTools;
import net.sf.jasperreports.engine.JRException;

import java.util.List;
import java.util.Map;

public class AdvancedEntityResultDataSource extends AbstractEntityResultDataSource<AdvancedEntityResult> {

    private final int totalSize;

    private final int pageSize;
    private int offset;

    private final Object bean;
    private final String method;
    private final Map<String, Object> reportParameters;
    private final List<Object> attributes;
    private final List<?> order;

    public AdvancedEntityResultDataSource(Object bean, String method, Map<String, Object> reportParameters,
                                          List<Object> attributes, int pageSize, int offset, List<?> order) {
        super((AdvancedEntityResult) ReflectionTools.invoke(bean, method, reportParameters,
                attributes, pageSize, offset, order));
        this.bean = bean;
        this.method = method;
        this.reportParameters = reportParameters;
        this.attributes = attributes;
        this.pageSize = pageSize;
        this.offset = offset;
        this.order = order;
        this.totalSize = this.result.getTotalRecordCount();
    }

    @Override
    public int calculateIndex() {
        return this.index - this.offset;
    }

    @Override
    public boolean next() throws JRException {
        this.index++;
        if (this.index >= this.totalSize) {
            return false;
        }
        if (this.index >= this.size + this.offset) {
            this.offset += pageSize;
            this.result = (AdvancedEntityResult) ReflectionTools.invoke(bean, method, reportParameters,
                    attributes, pageSize, offset, order);
        }
        return true;
    }

}
