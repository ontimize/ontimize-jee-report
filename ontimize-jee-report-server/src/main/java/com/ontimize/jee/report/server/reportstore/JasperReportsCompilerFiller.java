package com.ontimize.jee.report.server.reportstore;

import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.MapTools;
import com.ontimize.jee.common.tools.PathTools;
import com.ontimize.jee.report.common.exception.ReportStoreException;
import com.ontimize.jee.report.common.reportstore.ReportOutputType;
import com.ontimize.jee.report.common.reportstore.ReportParameter;
import com.ontimize.jee.report.common.services.IReportDefinition;
import net.lingala.zip4j.core.ZipFile;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The Class JasperReportsCompilerFiller.
 */
@SuppressWarnings("deprecation")
public class JasperReportsCompilerFiller implements IReportCompiler, IReportFiller, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(JasperReportsCompilerFiller.class);
    /**
     * The Constant PARAMETER_REPORT_CONNECTION.
     */
    private static final String PARAMETER_REPORT_CONNECTION = "REPORT_CONNECTION";

    /**
     * The Constant PARAMETER_REPORT_LOCALE.
     */
    private static final String PARAMETER_REPORT_LOCALE = "REPORT_LOCALE";

    /**
     * The Constant PARAMETER_REPORT_RESOURCE_BUNDLE.
     */
    private static final String PARAMETER_REPORT_RESOURCE_BUNDLE = "REPORT_RESOURCE_BUNDLE";

    /**
     * The Constant JASPER.
     */
    private static final String JASPER = ".jasper";

    /**
     * The Constant JRXML.
     */
    private static final String JRXML = ".jrxml";

    /**
     * The Constant TMP_PREFIX.
     */
    private static final String TMP_PREFIX = "OJEE_JRTMP";
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Instantiates a new jasper reports compiler filler.
     */
    public JasperReportsCompilerFiller() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.reportstore.IReportCompiler#compile(java.nio.file.Path, java.nio.file.Path)
     */
    @Override
    public IReportDefinition compile(final Path inputZip, final Path outputFolder, IReportDefinition rDef) throws ReportStoreCompileException {
        try {
            ZipFile zipFile = new ZipFile(inputZip.toFile());
            final Path temporaryFolder = Files.createTempDirectory(JasperReportsCompilerFiller.TMP_PREFIX);
            zipFile.extractAll(temporaryFolder.toString());

            Files.walkFileTree(temporaryFolder, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(JasperReportsCompilerFiller.JRXML)) {
                        try {
                            JasperCompileManager.compileReportToFile(file.toString(), outputFolder.resolve(file.getFileName() + JasperReportsCompilerFiller.JASPER).toString());
                            JasperReport report = JasperCompileManager.compileReport(file.toString());
                            JRParameter[] params = report.getParameters();

                            List<ReportParameter> reportParams = new ArrayList<ReportParameter>();
                            for (int i = 0; i < params.length; i++) {
                                if (params[i].isForPrompting() && !params[i].isSystemDefined()) {
                                    reportParams.add(new ReportParameter(params[i].getName(), params[i].getDescription(),
                                            params[i].getValueClassName(), params[i].getNestedTypeName()));
                                }
                            }
                            rDef.setParameters(reportParams);

                        } catch (JRException e) {
                            throw new IOException(e);
                        }

                    } else {
                        Files.copy(file, outputFolder.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    }
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
            try {
                PathTools.deleteFolder(temporaryFolder);
            } catch (Exception ex) {
                JasperReportsCompilerFiller.logger.error("error deleting temporary folder", ex);
            }
            return rDef;
        } catch (Exception error) {
            throw new ReportStoreCompileException(error);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.reportstore.IReportFiller#fillReport(com.ontimize.jee.common.services.reportstore.IReportDefinition, java.nio.file.Path, java.util.Map,
     * com.ontimize.jee.server.services.reportstore.ReportOutputType, java.lang.String, java.util.ResourceBundle, java.util.Locale)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public InputStream fillReport(IReportDefinition reportDefinition, Path compiledReportFolder, Map<String, Object> reportParameters, ReportOutputType outputType,
                                  String otherType, ResourceBundle bundle, Locale locale, String datasourceName) throws ReportStoreException {
        Connection con = null;
        DataSource dataSource = null;
        try {
            // Protect invalid input data
            CheckingTools.failIfNull(reportDefinition, "E_REQUIRED_REPORTDEFINITION", new Object[0]);
            CheckingTools.failIfNull(outputType, "E_REQUIRED_OUTPUTTYPE", new Object[0]);
            CheckingTools.failIfNull(compiledReportFolder, "E_REQUIRED_COMPILEDREPORTFOLDER", new Object[0]);
            CheckingTools.failIf(!Files.exists(compiledReportFolder), "E_REQUIRED_VALID_COMPILEDREPORTFOLDER", new Object[0]);
            reportParameters = reportParameters == null ? new HashMap<String, Object>() : reportParameters;

            if (datasourceName == null) {
                dataSource = this.applicationContext.getBean(DataSource.class); // TODO be carefull, can exists more than one
            } else {
                dataSource = this.applicationContext.getBean(datasourceName, DataSource.class);
            }
            if (!reportParameters.containsKey(JasperReportsCompilerFiller.PARAMETER_REPORT_CONNECTION)) {
                con = DataSourceUtils.getConnection(dataSource);
            }
            Map<String, Object> params = new HashMap<>();
            params = (Map) MapTools.union(params, reportDefinition.getOtherInfo());// Default report parameters
            params = (Map) MapTools.union(params, reportParameters);// Custom invocation parameters

            this.fillParameters(con, bundle, locale, params);// Extra parameters: Connection, bundle, locale...

            // TODO tal vez sea mejor a fichero por temas de memoria si se dispara un informe
            Path reportCompiled = compiledReportFolder.resolve(reportDefinition.getReportFileName() + JasperReportsCompilerFiller.JASPER);
            CheckingTools.failIf(!Files.exists(reportCompiled), "E_REQUIRED_VALID_COMPILEDREPORTFILE", new Object[0]);
            JasperPrint fillReport = JasperFillManager.fillReport(reportCompiled.toString(), params);

            return this.convertReport(outputType, otherType, fillReport);
        } catch (JRException | IOException error) {
            throw new ReportStoreException(error);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public InputStream fillReport(IReportDefinition reportDefinition, JasperReport compiledReport,
                                  Map<String, Object> reportParameters, ReportOutputType outputType, String otherType, ResourceBundle bundle,
                                  Locale locale, String dataSourceName) throws ReportStoreException {
        Connection con = null;
        DataSource dataSource = null;
        try {
            // Protect invalid input data
            CheckingTools.failIfNull(reportDefinition, "E_REQUIRED_REPORTDEFINITION", new Object[0]);
            CheckingTools.failIfNull(outputType, "E_REQUIRED_OUTPUTTYPE", new Object[0]);
            reportParameters = reportParameters == null ? new HashMap<String, Object>() : reportParameters;

            if (dataSourceName == null) {
                dataSource = this.applicationContext.getBean(DataSource.class); // TODO be carefull, can exists more than one
            } else {
                dataSource = this.applicationContext.getBean(dataSourceName, DataSource.class);
            }
            if (!reportParameters.containsKey(JasperReportsCompilerFiller.PARAMETER_REPORT_CONNECTION)) {
                con = DataSourceUtils.getConnection(dataSource);
            }
            Map<String, Object> params = new HashMap<>();
            params = (Map) MapTools.union(params, reportDefinition.getOtherInfo());// Default report parameters
            params = (Map) MapTools.union(params, reportParameters);// Custom invocation parameters

            this.fillParameters(con, bundle, locale, params);// Extra parameters: Connection, bundle, locale...

            JasperPrint fillReport = JasperFillManager.fillReport(compiledReport, params);

            return this.convertReport(outputType, otherType, fillReport);
        } catch (JRException | IOException error) {
            throw new ReportStoreException(error);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public InputStream fillReport(IReportDefinition reportDefinition, JasperReport compiledReport,
                                  Map<String, Object> reportParameters, ReportOutputType outputType, String otherType, ResourceBundle bundle,
                                  Locale locale, JRDataSource ods) throws ReportStoreException {
        Connection con = null;
        DataSource dataSource = null;
        try {
            // Protect invalid input data
            CheckingTools.failIfNull(reportDefinition, "E_REQUIRED_REPORTDEFINITION", new Object[0]);
            CheckingTools.failIfNull(outputType, "E_REQUIRED_OUTPUTTYPE", new Object[0]);
            reportParameters = reportParameters == null ? new HashMap<String, Object>() : reportParameters;
            dataSource = this.applicationContext.getBean(DataSource.class); // TODO be carefull, can exists more than one

            if (!reportParameters.containsKey(JasperReportsCompilerFiller.PARAMETER_REPORT_CONNECTION)) {
                con = DataSourceUtils.getConnection(dataSource);
            }
            Map<String, Object> params = new HashMap<>();
            params = (Map) MapTools.union(params, reportDefinition.getOtherInfo());// Default report parameters
            params = (Map) MapTools.union(params, reportParameters);// Custom invocation parameters

            this.fillParameters(con, bundle, locale, params);// Extra parameters: Connection, bundle, locale...

            JasperPrint fillReport = JasperFillManager.fillReport(compiledReport, params, ods);

            return this.convertReport(outputType, otherType, fillReport);
        } catch (JRException | IOException error) {
            throw new ReportStoreException(error);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.reportstore.IReportFiller#fillReport(com.ontimize.jee.common.services.reportstore.IReportDefinition, java.nio.file.Path,
     * com.ontimize.jee.server.services.reportstore.IReportService, java.util.Map, com.ontimize.jee.server.services.reportstore.ReportOutputType, java.lang.String,
     * java.util.ResourceBundle, java.util.Locale)
     */
    @Override
    public InputStream fillReport(IReportDefinition definition, Path compiledReportFolder, IReportAdapter service, Map<String, Object> parameters, ReportOutputType outputType,
                                  String otherType, ResourceBundle bundle, Locale locale, String datasourceName) throws ReportStoreException {
        try {
            service.beforeReportBegins(definition, parameters);
            return this.fillReport(definition, compiledReportFolder, parameters, outputType, otherType, bundle, locale, datasourceName);
        } catch (ReportStoreException ex) {
            service.onReportError(definition, parameters, ex);
            throw ex;
        } finally {
            service.afterReportEnds(definition, parameters);
        }
    }

    /**
     * Convert report.
     *
     * @param outputType the output type
     * @param otherType  the other type
     * @param fillReport the fill report
     * @return the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws JRException the JR exception
     */
    protected InputStream convertReport(final ReportOutputType outputType, final String otherType, final JasperPrint fillReport)
            throws IOException, JRException, ReportStoreException {
        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream os = new PipedOutputStream(in);

        // Try to do in another thread, due to PipedStreams may deadlock in same thread when buffer is full
        Thread thread = new Thread("convertReportThread") {

            @SuppressWarnings("incomplete-switch")
            @Override
            public void run() {
                try {
                    switch (outputType) {
                        case JASPER_REPORT:
                            JRSaver.saveObject(fillReport, os);
                            break;
                        case PDF:
                            JRPdfExporter pdfExp = new JRPdfExporter();
                            pdfExp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, Arrays.asList(new JasperPrint[]{fillReport}));
                            pdfExp.setParameter(JRExporterParameter.OUTPUT_STREAM, os);

                            pdfExp.setParameter(JRPdfExporterParameter.FORCE_LINEBREAK_POLICY, Boolean.TRUE);
                            pdfExp.setParameter(JRPdfExporterParameter.FORCE_SVG_SHAPES, Boolean.TRUE);
                            pdfExp.setParameter(JRPdfExporterParameter.IS_COMPRESSED, Boolean.TRUE);
                            pdfExp.exportReport();
                            break;
                        case DOCX:
                            JRDocxExporter docxExp = new JRDocxExporter();
                            docxExp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, Arrays.asList(new JasperPrint[]{fillReport}));
                            docxExp.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
                            docxExp.exportReport();
                            break;
                        case XLSX:
                            JRXlsxExporter xlsxExp = new JRXlsxExporter();
                            xlsxExp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, Arrays.asList(new JasperPrint[]{fillReport}));
                            xlsxExp.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
                            xlsxExp.exportReport();
                            break;
//						case HTML:
//							JRHtmlExporter htmlExp = new JRHtmlExporter();
//							htmlExp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, Arrays.asList(new JasperPrint[] { fillReport }));
//							htmlExp.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
//							htmlExp.exportReport();
//							break;
                        case OTHER:
                            // TODO
                            break;
                    }
                } catch (Exception ex) {
                    JasperReportsCompilerFiller.logger.error("E_EXPORTING_REPORT", ex);
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

    /**
     * Fill parameters.
     *
     * @param con    the con
     * @param bundle the bundle
     * @param locale the locale
     * @param params the params
     */
    private void fillParameters(Connection con, ResourceBundle bundle, Locale locale, Map<String, Object> params) {
        if (!params.containsKey(JasperReportsCompilerFiller.PARAMETER_REPORT_CONNECTION) && (con != null)) {
            params.put(JasperReportsCompilerFiller.PARAMETER_REPORT_CONNECTION, con);
        }

        if (!params.containsKey(JasperReportsCompilerFiller.PARAMETER_REPORT_LOCALE) && (locale != null)) {
            params.put(JasperReportsCompilerFiller.PARAMETER_REPORT_LOCALE, locale);
        }
        if (!params.containsKey(JasperReportsCompilerFiller.PARAMETER_REPORT_RESOURCE_BUNDLE) && (bundle != null)) {
            params.put(JasperReportsCompilerFiller.PARAMETER_REPORT_RESOURCE_BUNDLE, bundle);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


}
