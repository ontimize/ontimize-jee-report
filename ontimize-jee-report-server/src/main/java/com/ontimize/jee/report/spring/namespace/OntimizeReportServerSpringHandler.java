package com.ontimize.jee.report.spring.namespace;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class OntimizeReportServerSpringHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		this.registerBeanDefinitionParser("ontimize-report-configuration", new OntimizeReportConfigurationBeanDefinitionParser());
	}

}
