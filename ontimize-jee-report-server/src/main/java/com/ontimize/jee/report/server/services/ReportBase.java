package com.ontimize.jee.report.server.services;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.core.layout.LayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;

import com.ontimize.jee.common.db.NullValue;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.db.SQLStatementBuilder.Expression;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.report.common.dto.ColumnDto;
import com.ontimize.jee.report.common.dto.FunctionTypeDto;
import com.ontimize.jee.report.common.dto.OrderByDto;
import com.ontimize.jee.report.common.dto.StyleParamsDto;
import com.ontimize.jee.report.common.exception.DynamicReportException;
import com.ontimize.jee.report.common.util.FilterParameter;
import com.ontimize.jee.server.rest.BasicExpressionProcessor;

import com.ontimize.jee.server.rest.ORestController;
import com.ontimize.jee.server.rest.ParseUtilsExt;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class ReportBase {

    protected static final Log log = LogFactory.getLog(ReportBase.class);
    protected JasperPrint jp;
    protected JasperReport jr;
    protected Map params = new HashMap();

    public abstract DynamicReport buildReport(List<ColumnDto> columnsDto, String title, List<String> groups,
            String entity, String service, String path, Boolean vertical, List<FunctionTypeDto> functions,
            StyleParamsDto style, String subtitle, String language) throws DynamicReportException;

    public abstract JRDataSource getDataSource(List<ColumnDto> columns, List<String> groups, List<OrderByDto> orderBy,
            String entity, String service, String path, FilterParameter filters) throws DynamicReportException;

    public InputStream generateReport(List<ColumnDto> columns, String title, List<String> groups, String entity,
            String service, String path, Boolean vertical, List<FunctionTypeDto> functions, StyleParamsDto style,
            String subtitle, List<OrderByDto> orderBy, String language, FilterParameter filters)
            throws DynamicReportException {

        DynamicReport dr = buildReport(columns, title, groups, entity, service, path, vertical, functions, style,
                subtitle, language);

        processFilterParameter(filters);

        /**
         * We obtain the data source based on a collection of objects
         */
        JRDataSource ds = getDataSource(columns, groups, orderBy, entity, service, path, filters);

        /**
         * We create the JasperReport object that we pass as a parameter to
         * DynamicReport, along with a new instance of ClassicLayoutManager and the
         * JRDataSource
         */
        try {
            jr = DynamicJasperHelper.generateJasperReport(dr, getLayoutManager(), params);
        } catch (JRException e) {
            log.error(e);
            throw new DynamicReportException("Impossible to generate Jasper Report!", e);
        }

        /**
         * We create the object that we will print passing as a parameter the
         * JasperReport object, and the JRDataSource
         */
        try {
            log.debug("Filling the report");
            if (ds != null) {
                jp = JasperFillManager.fillReport(jr, params, ds);
            } else {
                jp = JasperFillManager.fillReport(jr, params);
            }
            log.debug("Filling done!");
        } catch (JRException e) {
            log.error(e);
            throw new DynamicReportException("Impossible to fill Jasper Report!", e);
        }
        InputStream is = null;
        try {
            is = convertReport(jp);
        } catch (IOException e) {
            log.error(e);
            throw new DynamicReportException("Impossible to convert Jasper Report!", e);
        }
        return is;
    }

    protected LayoutManager getLayoutManager() {
        return new ClassicLayoutManager();
    }

    protected void processFilterParameter(final FilterParameter filterParam) {

        Map<?, ?> kvQueryParameter = filterParam.getFilter();
        List<?> avQueryParameter = filterParam.getColumns();
        HashMap<?, ?> hSqlTypes = filterParam.getSqltypes();

        Map<Object, Object> processedKeysValues = this.createKeysValues(kvQueryParameter, hSqlTypes);
        List<Object> processedAttributesValues = this.createAttributesValues(avQueryParameter, hSqlTypes);
        filterParam.setKv(processedKeysValues);
        filterParam.setColumns(processedAttributesValues);

    }

    protected Map<Object, Object> createKeysValues(Map<?, ?> kvQueryParam, Map<?, ?> hSqlTypes) {
        Map<Object, Object> kv = new HashMap<>();
        if ((kvQueryParam == null) || kvQueryParam.isEmpty()) {
            return kv;
        }

        if (kvQueryParam.containsKey(ORestController.BASIC_EXPRESSION)) {
            Object basicExpressionValue = kvQueryParam.remove(ORestController.BASIC_EXPRESSION);
            this.processBasicExpression(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY, kv,
                    basicExpressionValue, hSqlTypes);
        }

        if (kvQueryParam.containsKey(ORestController.FILTER_EXPRESSION)) {
            Object basicExpressionValue = kvQueryParam.remove(ORestController.FILTER_EXPRESSION);
            this.processBasicExpression(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.FILTER_KEY, kv,
                    basicExpressionValue, hSqlTypes);
        }

        for (Entry<?, ?> next : kvQueryParam.entrySet()) {
            Object key = next.getKey();
            Object value = next.getValue();
            if ((hSqlTypes != null) && hSqlTypes.containsKey(key)) {
                int sqlType = (Integer) hSqlTypes.get(key);
                value = ParseUtilsExt.getValueForSQLType(value, sqlType);
                if (value == null) {
                    if (ParseUtilsExt.BASE64 == sqlType) {
                        sqlType = Types.BINARY;
                    }
                    value = new NullValue(sqlType);
                }
            } else if (value == null) {
                value = new NullValue();
            }
            kv.put(key, value);
        }
        return kv;
    }

    protected List<Object> createAttributesValues(List<?> avQueryParam, Map<?, ?> hSqlTypes) {
        List<Object> av = new ArrayList<>();
        if ((avQueryParam == null) || avQueryParam.isEmpty()) {
            av.add("*");
            return av;
        }
        av.addAll(avQueryParam);
        return av;
    }

    protected void processBasicExpression(String key, Map<?, ?> keysValues, Object basicExpression,
            Map<?, ?> hSqlTypes) {
        if (basicExpression instanceof Map) {
            try {
                BasicExpression bE = BasicExpressionProcessor.getInstance().processBasicEspression(basicExpression,
                        hSqlTypes);
                ((Map<Object, Object>) keysValues).put(key, bE);
            } catch (Exception error) {

            }
        }

    }

    protected InputStream convertReport(final JasperPrint fillReport) throws IOException {
        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream os = new PipedOutputStream(in);

        // Try to do in another thread, due to PipedStreams may deadlock in same thread
        // when buffer is full
        Thread thread = new Thread("convertReportThread") {

            @Override
            public void run() {
                try {

                    JRPdfExporter pdfExp = new JRPdfExporter();
                    pdfExp.setParameter(JRExporterParameter.JASPER_PRINT_LIST,
                            Arrays.asList(new JasperPrint[] { fillReport }));
                    pdfExp.setParameter(JRExporterParameter.OUTPUT_STREAM, os);

                    pdfExp.setParameter(JRPdfExporterParameter.FORCE_LINEBREAK_POLICY, Boolean.TRUE);
                    pdfExp.setParameter(JRPdfExporterParameter.FORCE_SVG_SHAPES, Boolean.TRUE);
                    pdfExp.setParameter(JRPdfExporterParameter.IS_COMPRESSED, Boolean.TRUE);
                    pdfExp.exportReport();

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                } finally {
                    try {
                        os.close();
                    } catch (IOException e) {
                    }
                }

            }

            ;
        };
        thread.start();
        return in;
    }

}
