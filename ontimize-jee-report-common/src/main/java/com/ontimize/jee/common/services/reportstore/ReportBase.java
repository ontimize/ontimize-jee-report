package com.ontimize.jee.common.services.reportstore;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.core.layout.LayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;

public abstract class ReportBase {

	protected static final Log log = LogFactory.getLog(ReportBase.class);
	protected JasperPrint jp;
	protected JasperReport jr;
	protected Map params = new HashMap();
	protected DynamicReport dr;

	public abstract DynamicReport buildReport(List<String> colums, String title, List<String> groups, String entity,
			String service, String orientation, List<String> functions, List<String> styleFunctions, String subtitle,
			List<ColumnStyleParamsDto> columnStyle) throws Exception;

	public abstract JRDataSource getDataSource(List<String> columns, String entity, String service)
			throws JRException, NoSuchFieldException, SecurityException, ClassNotFoundException;

	public InputStream generateReport(List<String> columns, String title, List<String> groups, String entity,
			String service, String orientation, List<String> functions, List<String> styleFunctions, String subtitle,
			List<ColumnStyleParamsDto> columnStyle) throws Exception {
		dr.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
		dr = buildReport(columns, title, groups, entity, service, orientation, functions, styleFunctions, subtitle,
				columnStyle);

		/**
		 * We obtain the data source based on a collection of objects
		 */
		JRDataSource ds = getDataSource(columns, entity, service);

		/**
		 * We create the JasperReport object that we pass as a parameter to
		 * DynamicReport, along with a new instance of ClassicLayoutManager and the
		 * JRDataSource
		 */
		jr = DynamicJasperHelper.generateJasperReport(dr, getLayoutManager(), params);

		/**
		 * We create the object that we will print passing as a parameter the
		 * JasperReport object, and the JRDataSource
		 */
		log.debug("Filling the report");
		if (ds != null) {
			jp = JasperFillManager.fillReport(jr, params, ds);
		} else {
			jp = JasperFillManager.fillReport(jr, params);
			log.debug("Filling done!");
			log.debug("Exporting the report (pdf, xls, etc)");
		}
		InputStream is = convertReport(jp);
		// exportReport();

		log.debug("test finished");
		return is;

	}

	protected LayoutManager getLayoutManager() {
		return new ClassicLayoutManager();
	}

	protected InputStream convertReport(final JasperPrint fillReport)
			throws IOException, JRException, ReportStoreException {
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

			};
		};
		thread.start();
		return in;
	}

}
