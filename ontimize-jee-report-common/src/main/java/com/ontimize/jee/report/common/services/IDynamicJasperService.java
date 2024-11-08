package com.ontimize.jee.report.common.services;

import ar.com.fdvs.dj.domain.DynamicReport;
import com.ontimize.jee.report.common.dto.FunctionParamsDto;
import com.ontimize.jee.report.common.dto.FunctionTypeDto;
import com.ontimize.jee.report.common.dto.ReportParamsDto;
import com.ontimize.jee.report.common.exception.DynamicReportException;

import java.io.InputStream;
import java.util.List;

public interface IDynamicJasperService {

    public DynamicReport buildReport(ReportParamsDto reportParamsDto) throws DynamicReportException;

    public InputStream createReport(ReportParamsDto reportParamsDto) throws DynamicReportException;

    public List<FunctionTypeDto> getFunctionsName(FunctionParamsDto params) throws DynamicReportException;
}
