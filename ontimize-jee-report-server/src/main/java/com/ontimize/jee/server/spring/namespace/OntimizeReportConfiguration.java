package com.ontimize.jee.server.spring.namespace;

import com.ontimize.jee.server.services.reportstore.ReportStoreConfiguration;

public class OntimizeReportConfiguration {

	private ReportStoreConfiguration reportStoreConfiguration;

	public ReportStoreConfiguration getReportStoreConfiguration() {
		return this.reportStoreConfiguration;
	}

	public void setReportStoreConfiguration(ReportStoreConfiguration reportStoreConfiguration) {
		this.reportStoreConfiguration = reportStoreConfiguration;
	}
}
