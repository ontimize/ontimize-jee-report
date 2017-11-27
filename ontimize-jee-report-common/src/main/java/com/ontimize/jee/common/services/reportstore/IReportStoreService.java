package com.ontimize.jee.common.services.reportstore;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * The Interface IReportStoreService.
 */
public interface IReportStoreService {

	/**
	 * Adds the report.
	 *
	 * @param rDef
	 *            the r def
	 * @param reportSource
	 *            the report sources in zip format
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	void addReport(IReportDefinition rDef, InputStream reportSource) throws ReportStoreException;

	/**
	 * Update report definition.
	 *
	 * @param reportId
	 *            the report id
	 * @param rDef
	 *            the r def
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	void updateReportDefinition(IReportDefinition rDef) throws ReportStoreException;

	/**
	 * Update report source.
	 *
	 * @param reportId
	 *            the report id
	 * @param is
	 *            the is report sources in zip format
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	void updateReportSource(Object reportId, InputStream is) throws ReportStoreException;

	/**
	 * Removes the report.
	 *
	 * @param reportId
	 *            the report id
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	void removeReport(Object reportId) throws ReportStoreException;

	/**
	 * Gets the report definition.
	 *
	 * @param reportId
	 *            the report id
	 * @return the report definition
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	IReportDefinition getReportDefinition(Object reportId) throws ReportStoreException;

	/**
	 * Gets the report source.
	 *
	 * @param reportId
	 *            the report id
	 * @return the report sources in zip format
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	InputStream getReportSource(Object reportId) throws ReportStoreException;

	/**
	 * Gets the report compiled.
	 *
	 * @param reportId
	 *            the report id
	 * @return the report compiled in zip format
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	InputStream getReportCompiled(Object reportId) throws ReportStoreException;

	/**
	 * List all reports.
	 *
	 * @return the collection
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	Collection<IReportDefinition> listAllReports() throws ReportStoreException;

	/**
	 * List reports of type.
	 *
	 * @param type
	 *            the type
	 * @return the collection
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	Collection<IReportDefinition> listReportsOfType(String type) throws ReportStoreException;

	/**
	 * Fill report.
	 *
	 * @param reportId
	 *            the report id
	 * @param reportParameters
	 *            the report parameters
	 * @param outputType
	 *            the output type
	 * @param otherType
	 *            the other type if ReportOutputType other is selected
	 * @return the input stream
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	InputStream fillReport(Object reportId, Map<String, Object> reportParameters, String dataSourceName, ReportOutputType outputType, String otherType) throws ReportStoreException;

	/**
	 * Fill report.
	 *
	 * @param reportId
	 *            the report id
	 * @param serviceName
	 *            the service name (must be an IReportAdapter)
	 * @param methodName
	 *            the method name
	 * @param methodParameters
	 *            the method parameters
	 * @param outputType
	 *            the output type
	 * @param otherType
	 *            the other type
	 * @return the input stream
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	InputStream fillReport(Object reportId, String serviceName, Map<String, Object> reportParameters, String dataSourceName, ReportOutputType outputType, String otherType)
	        throws ReportStoreException;

	/**
	 * Force to compile this report.
	 *
	 * @param reportId
	 * @throws ReportStoreException
	 *             when some problems accesing report or during compilation.
	 */
	void compileReport(Object reportId) throws ReportStoreException;
}