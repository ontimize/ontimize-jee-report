package com.ontimize.jee.report.spring.namespace;

import com.ontimize.jee.report.server.reportstore.ReportStoreConfiguration;

public class OntimizeReportConfiguration {

	private ReportStoreConfiguration reportStoreConfiguration;

	public ReportStoreConfiguration getReportStoreConfiguration() {
		return this.reportStoreConfiguration;
	}

	public void setReportStoreConfiguration(ReportStoreConfiguration reportStoreConfiguration) {
		this.reportStoreConfiguration = reportStoreConfiguration;
	}
}
