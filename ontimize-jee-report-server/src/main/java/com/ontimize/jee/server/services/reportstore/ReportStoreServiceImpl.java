package com.ontimize.jee.server.services.reportstore;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.ontimize.jee.common.services.reportstore.IReportDefinition;
import com.ontimize.jee.common.services.reportstore.ReportOutputType;
import com.ontimize.jee.common.services.reportstore.ReportStoreException;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.spring.namespace.OntimizeReportConfiguration;

/**
 * The Class ReportStoreServiceImpl.
 */
@Service("ReportStoreService")
@Lazy(value = true)
public class ReportStoreServiceImpl implements IReportStoreServiceServer, ApplicationContextAware {

	/** The implementation. */
	protected IReportStoreEngine	implementation	= null;

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.report.IReportStoreService#addReport(com.ontimize.jee.server.services.report.IReportDefinition)
	 */
	@Override
	public void addReport(IReportDefinition rDef, InputStream reportSource) throws ReportStoreException {
		this.getImplementation().addReport(rDef, reportSource);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.report.IReportStoreService#updateReportDefinition(java.lang.Object, com.ontimize.jee.server.services.report.IReportDefinition)
	 */
	@Override
	public void updateReportDefinition(IReportDefinition rDef) throws ReportStoreException {
		this.getImplementation().updateReportDefinition(rDef);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.report.IReportStoreService#updateReportSource(java.lang.Object, java.io.InputStream)
	 */
	@Override
	public void updateReportSource(Object reportId, InputStream is) throws ReportStoreException {
		this.getImplementation().updateReportSource(reportId, is);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.report.IReportStoreService#removeReport(java.lang.Object)
	 */
	@Override
	public void removeReport(Object reportId) throws ReportStoreException {
		this.getImplementation().removeReport(reportId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.report.IReportStoreService#getReportDefinition(java.lang.Object)
	 */
	@Override
	public IReportDefinition getReportDefinition(Object reportId) throws ReportStoreException {
		return this.getImplementation().getReportDefinition(reportId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.report.IReportStoreService#getReportSource(java.lang.Object)
	 */
	@Override
	public InputStream getReportSource(Object reportId) throws ReportStoreException {
		return this.getImplementation().getReportSource(reportId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.report.IReportStoreService#getReportCompiled(java.lang.Object)
	 */
	@Override
	public InputStream getReportCompiled(Object reportId) throws ReportStoreException {
		return this.getImplementation().getReportCompiled(reportId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.report.IReportStoreService#listAllReports()
	 */
	@Override
	public Collection<IReportDefinition> listAllReports() throws ReportStoreException {
		return this.getImplementation().listAllReports();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.report.IReportStoreService#listReportsOfType(java.lang.String)
	 */
	@Override
	public Collection<IReportDefinition> listReportsOfType(String type) throws ReportStoreException {
		return this.getImplementation().listReportsOfType(type);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.report.IReportStoreService#fillReport(java.lang.Object, java.util.Map, com.ontimize.jee.server.services.report.ReportOutputType,
	 * java.lang.String)
	 */
	@Override
	public InputStream fillReport(Object reportId, Map<String, Object> reportParameters, String dataSourceName, ReportOutputType outputType, String otherType)
			throws ReportStoreException {
		return this.getImplementation().fillReport(reportId, reportParameters, dataSourceName, outputType, otherType);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.report.IReportStoreService#fillReport(java.lang.Object, java.lang.String, java.lang.String, java.util.Map,
	 * com.ontimize.jee.server.services.report.ReportOutputType, java.lang.String)
	 */
	@Override
	public InputStream fillReport(Object reportId, String serviceName, Map<String, Object> reportParameters, String dataSourceName, ReportOutputType outputType, String otherType)
			throws ReportStoreException {
		return this.getImplementation().fillReport(reportId, serviceName, reportParameters, dataSourceName, outputType, otherType);
	}

	@Override
	public void compileReport(Object reportId) throws ReportStoreException {
		this.getImplementation().compileReport(reportId);
	}

	/**
	 * Gets the implementation.
	 *
	 * @return the implementation
	 */
	protected IReportStoreEngine getImplementation() {
		CheckingTools.failIfNull(this.implementation, "Not implementation defined for report store.");
		return this.implementation;
	}

	/**
	 * (non-Javadoc).
	 *
	 * @param applicationContext
	 *            the new application context
	 * @throws BeansException
	 *             the beans exception
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.implementation = applicationContext.getBean(OntimizeReportConfiguration.class).getReportStoreConfiguration().getEngine();
	}

	@Override
	public void updateSettings() {
		this.getImplementation().updateSettings();

	}
}
