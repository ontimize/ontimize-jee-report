package com.ontimize.jee.report.common.services;

import ar.com.fdvs.dj.domain.DynamicReport;
import com.ontimize.jee.report.common.dto.*;
import com.ontimize.jee.report.common.exception.DynamicReportException;

import java.io.InputStream;
import java.util.List;

public interface IDynamicJasperService {

    public DynamicReport buildReport(List<ColumnDto> columns, String title, List<String> groups, String entity,
                                     String service, Boolean vertical, List<FunctionTypeDto> functions, StyleParamsDto styles, String subtitle,
                                     String language)
            throws DynamicReportException;

    public InputStream createReport(ReportParamsDto param) throws DynamicReportException;

    public List<String> getFunctionsName(FunctionParamsDto params) throws DynamicReportException;
}
