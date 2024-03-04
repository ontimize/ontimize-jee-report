package com.ontimize.jee.report.server.reportstore;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.report.common.exception.ReportStoreException;
import com.ontimize.jee.report.common.services.IReportDefinition;

public interface IReportStoreEngine extends IReportStoreServiceServer {

    EntityResult updateReportDefinition(IReportDefinition rDef) throws ReportStoreException;

}
