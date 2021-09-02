package com.ontimize.jee.common.services.reportstore;

import java.io.InputStream;
import java.util.List;

import ar.com.fdvs.dj.domain.DynamicReport;

public interface IDynamicJasperService {

	public DynamicReport buildReport(List<String> columns, String title, List<String> groups, String entity,
			String service, String orientation, List<String> functions, List<String> styleFunctions, String subtitle,
			List<ColumnStyleParamsDto> columnStyle) throws Exception;

	public InputStream createReport(List<String> colums, String title, List<String> groups, String entity,
			String service, String orientation, List<String> functions, List<String> styleFunctions, String subtitle,
			List<ColumnStyleParamsDto> columnStyle) throws Exception;

	public List<String> getFunctions(String entity, String service, List<String> columns);
}
