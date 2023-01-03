package com.ontimize.jee.report.spring.namespace;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class OntimizeReportConfigurationBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    /**
     * The Constant SCOPE.
     */
    @SuppressWarnings("unused")
    private static final String SCOPE = "scope";

    @Override
    protected Class<?> getBeanClass(Element element) {
        return OntimizeReportConfiguration.class;
    }

    @Override
    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder builder) {

        // We want any parsing to occur as a child of this tag so we need to
        // make
        // a new one that has this as it's owner/parent
        ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(), builder.getBeanDefinition());

        // Support for report
        Element report = DomUtils.getChildElementByTagName(element, "report");
        if (report != null) {
            // Just make a new Parser for each one and let the parser do the
            // work
            ReportBeanDefinitionParser ro = new ReportBeanDefinitionParser();
            builder.addPropertyValue("reportStoreConfiguration", ro.parse(report, nestedCtx));
        }
        builder.setLazyInit(true);
    }

}
