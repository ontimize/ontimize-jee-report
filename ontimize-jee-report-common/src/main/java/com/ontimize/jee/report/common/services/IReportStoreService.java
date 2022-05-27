package com.ontimize.jee.report.common.services;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.report.common.exception.ReportStoreException;
import com.ontimize.jee.report.common.reportstore.ReportOutputType;

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
	EntityResult addReport(IReportDefinition rDef, InputStream reportSource) throws ReportStoreException;

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
	EntityResult updateReportDefinition(IReportDefinition rDef) throws ReportStoreException;

	/**
	 * Removes the report.
	 *
	 * @param reportId
	 *            the report id
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	EntityResult removeReport(Object reportId) throws ReportStoreException;

	/**
	 * Gets the report definition.
	 *
	 * @param reportId
	 *            the report id
	 * @return the report definition
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	EntityResult getReportDefinition(Object reportId) throws ReportStoreException;

	/**
	 * List all reports.
	 *
	 * @return the collection
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	EntityResult listAllReports() throws ReportStoreException;

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
	CompletableFuture<EntityResult> fillReport(Object reportId, Map<String, Object> reportParameters, String dataSourceName, ReportOutputType outputType, String otherType, Map<Object, Object> keysValues)
			throws ReportStoreException;

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
	CompletableFuture<EntityResult> fillReport(Object reportId, String serviceName, Map<String, Object> reportParameters, String dataSourceName, ReportOutputType outputType, String otherType)
	        throws ReportStoreException;

}