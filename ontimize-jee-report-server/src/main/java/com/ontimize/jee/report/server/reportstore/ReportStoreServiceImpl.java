package com.ontimize.jee.report.server.reportstore;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.report.common.exception.ReportStoreException;
import com.ontimize.jee.report.common.reportstore.ReportOutputType;
import com.ontimize.jee.report.common.services.IReportDefinition;
import com.ontimize.jee.report.spring.namespace.OntimizeReportConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The Class ReportStoreServiceImpl.
 */
@Service("ReportStoreService")
@Lazy(value = true)
public class ReportStoreServiceImpl implements IReportStoreServiceServer, ApplicationContextAware {

    /**
     * The implementation.
     */
    protected IReportStoreEngine implementation = null;

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.report.IReportStoreService#addReport(com.ontimize.jee.server.services.report.IReportDefinition)
     */
    @Override
    public EntityResult addReport(IReportDefinition rDef, InputStream reportSource) throws ReportStoreException {
        return this.getImplementation().addReport(rDef, reportSource);
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.report.IReportStoreService#updateReportDefinition(java.lang.Object, com.ontimize.jee.server.services.report.IReportDefinition)
     */
    @Override
    public EntityResult updateReportDefinition(IReportDefinition rDef, InputStream reportSource) throws ReportStoreException, IOException {
        return this.getImplementation().updateReportDefinition(rDef, reportSource);
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.report.IReportStoreService#removeReport(java.lang.Object)
     */
    @Override
    public EntityResult removeReport(Object reportId) throws ReportStoreException {
        return this.getImplementation().removeReport(reportId);
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.report.IReportStoreService#getReportDefinition(java.lang.Object)
     */
    @Override
    public EntityResult getReportDefinition(Object reportId) throws ReportStoreException {
        return this.getImplementation().getReportDefinition(reportId);
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.report.IReportStoreService#listAllReports()
     */
    @Override
    public EntityResult listAllReports() throws ReportStoreException {
        return this.getImplementation().listAllReports();
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.report.IReportStoreService#fillReport(java.lang.Object, java.util.Map, com.ontimize.jee.server.services.report.ReportOutputType,
     * java.lang.String)
     */
    @Override
    @Async("ReportExecutor")
    public CompletableFuture<EntityResult> fillReport(Object reportId, Map<String, Object> reportParameters, String dataSourceName, ReportOutputType outputType, String otherType, Map<Object, Object> keysValues)
            throws ReportStoreException {
        return this.getImplementation().fillReport(reportId, reportParameters, dataSourceName, outputType, otherType, keysValues);
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.report.IReportStoreService#fillReport(java.lang.Object, java.lang.String, java.lang.String, java.util.Map,
     * com.ontimize.jee.server.services.report.ReportOutputType, java.lang.String)
     */
    @Override
    @Async("ReportExecutor")
    public CompletableFuture<EntityResult> fillReport(Object reportId, String serviceName, Map<String, Object> reportParameters, String dataSourceName, ReportOutputType outputType, String otherType)
            throws ReportStoreException {
        return this.getImplementation().fillReport(reportId, serviceName, reportParameters, dataSourceName, outputType, otherType);
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
     * @param applicationContext the new application context
     * @throws BeansException the beans exception
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
