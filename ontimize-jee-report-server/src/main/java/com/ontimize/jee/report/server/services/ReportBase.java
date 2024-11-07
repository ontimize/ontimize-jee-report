package com.ontimize.jee.report.server.services;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.core.layout.LayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import com.ontimize.jee.report.common.dto.ColumnDto;
import com.ontimize.jee.report.common.dto.OrderByDto;
import com.ontimize.jee.report.common.dto.ReportParamsDto;
import com.ontimize.jee.report.common.exception.DynamicReportException;
import com.ontimize.jee.server.rest.FilterParameter;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ReportBase {

    protected static final Log log = LogFactory.getLog(ReportBase.class);
    protected JasperPrint jp;
    protected JasperReport jr;
    protected Map params = new HashMap();

    public ReportBase() {
        // no-op
    }
    public abstract DynamicReport buildReport(final ReportParamsDto reportParamsDto) throws DynamicReportException;

    public abstract JRDataSource getDataSource(List<ColumnDto> columns, List<String> groups, List<OrderByDto> orderBy,
            String entity, String service, String path, FilterParameter filters, Boolean advQuery)
            throws DynamicReportException;

    public InputStream generateReport(final ReportParamsDto reportParamsDto)
        throws DynamicReportException {

        DynamicReport dr = buildReport(reportParamsDto);

        /**
         * We obtain the data source based on a collection of objects
         */
        List<ColumnDto> columns = reportParamsDto.getColumns();
        List<String> groups = reportParamsDto.getGroups();
        List<OrderByDto> orderBy = reportParamsDto.getOrderBy();
        String entity = reportParamsDto.getEntity();
        String service = reportParamsDto.getService();
        String path = reportParamsDto.getPath();
        FilterParameter filters = reportParamsDto.getFilters();
        Boolean advQuery = reportParamsDto.getAdvQuery();
        JRDataSource ds = getDataSource(columns, groups, orderBy, entity, service, path, filters, advQuery);

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
