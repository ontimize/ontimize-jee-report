/*
 *
 */
package com.ontimize.jee.report.server.reportstore;

import com.ontimize.jee.report.common.exception.ReportStoreException;
import com.ontimize.jee.report.common.reportstore.ReportOutputType;
import com.ontimize.jee.report.common.services.IReportDefinition;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperReport;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The Interface IReportFiller.
 */
public interface IReportFiller {

    /**
     * Fill report.
     *
     * @param reportDefinition     the report definition
     * @param compiledReportFolder the compiled report folder
     * @param reportParameters     the report parameters
     * @param outputType           the output type
     * @param otherType            the other type
     * @param bundle               the bundle
     * @param locale               the locale
     * @param datasourceName       the datasource name
     * @return the input stream
     * @throws ReportStoreException the report store exception
     */
    InputStream fillReport(IReportDefinition reportDefinition, Path compiledReportFolder, Map<String, Object> reportParameters, ReportOutputType outputType, String otherType,
                           ResourceBundle bundle, Locale locale, String datasourceName) throws ReportStoreException;

    /**
     * Fill report.
     *
     * @param reportDefinition     the report definition
     * @param compiledReportFolder the compiled report folder
     * @param serviceName          the service name
     * @param methodName           the method name
     * @param methodParameters     the method parameters
     * @param outputType           the output type
     * @param otherType            the other type
     * @param bundle               the bundle
     * @param locale               the locale
     * @return the input stream
     * @throws ReportStoreException the report store exception
     */
    InputStream fillReport(IReportDefinition reportDefinition, Path compiledReportFolder, IReportAdapter service, Map<String, Object> methodParameters, ReportOutputType outputType,
                           String otherType, ResourceBundle bundle, Locale locale, String datasourceName) throws ReportStoreException;

    InputStream fillReport(IReportDefinition reportDefinition, JasperReport compiledReport,
                           Map<String, Object> reportParameters, ReportOutputType outputType, String otherType, ResourceBundle bundle,
                           Locale locale, String dataSourceName) throws ReportStoreException;

    InputStream fillReport(IReportDefinition reportDefinition, JasperReport compiledReport,
                           Map<String, Object> reportParameters, ReportOutputType outputType, String otherType, ResourceBundle bundle,
                           Locale locale, JRDataSource ods) throws ReportStoreException;

}
