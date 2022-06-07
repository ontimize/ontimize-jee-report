package com.ontimize.jee.report.common.services;

import java.io.InputStream;
import java.util.List;

import com.ontimize.jee.report.common.dto.ColumnDto;
import com.ontimize.jee.report.common.dto.FunctionParamsDto;
import com.ontimize.jee.report.common.dto.ReportParamsDto;
import com.ontimize.jee.report.common.dto.ServiceRendererDto;
import com.ontimize.jee.report.common.exception.DynamicReportException;

import ar.com.fdvs.dj.domain.DynamicReport;

public interface IDynamicJasperService {

	public DynamicReport buildReport(List<String> columns, String title, List<String> groups, String entity,
			String service, Boolean vertical, List<String> functions, List<String> style, String subtitle,
			List<ColumnDto> columnsDto, String language, List<ServiceRendererDto> serviceRendererList)
			throws DynamicReportException;

	public InputStream createReport(ReportParamsDto param) throws DynamicReportException;

	public List<String> getFunctionsName(FunctionParamsDto params) throws DynamicReportException;
}
