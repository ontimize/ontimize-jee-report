package com.ontimize.jee.report.spring.namespace;

import java.util.List;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ontimize.jee.report.server.reportstore.FileReportStoreEngine;
import com.ontimize.jee.report.server.reportstore.JasperReportsCompilerFiller;
import com.ontimize.jee.report.server.reportstore.ReportStoreConfiguration;

/**
 * The Class RemotePreferencesBeanDefinitionParser.
 */
public class ReportBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	/** The Constant SCOPE. */
	@SuppressWarnings("unused")
	private static final String SCOPE = "scope";

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
	 */
	@Override
	protected Class<?> getBeanClass(Element element) {
		return ReportStoreConfiguration.class;
	}

	/**
	 * Called when the remotePreferences tag is to be parsed.
	 *
	 * @param element
	 *            The tag element
	 * @param ctx
	 *            The context in which the parsing is occuring
	 * @param builder
	 *            The bean definitions build to use
	 */
	@Override
	protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder builder) {
		Element child = DomUtils.getChildElements(element).get(0);
		Object engine = null;
		if ("default-report-engine".equals(child.getLocalName())) {
			final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(), builder.getBeanDefinition());
			engine = new ReportEngineParser().parse(child, nestedCtx);
			((AbstractBeanDefinition) engine).setDependencyCheck(AbstractBeanDefinition.DEPENDENCY_CHECK_NONE);
		} else {
			// construimos el bean que nos venga que deberia ser un IReportStoreEngine
			//engine = DefinitionParserUtil.parseNode(child, ctx, builder.getBeanDefinition(), element.getAttribute(ReportBeanDefinitionParser.SCOPE), false);
		}
		builder.addPropertyValue("engine", engine);
		builder.setLazyInit(true);
	}

	public static class ReportEngineParser extends AbstractSingleBeanDefinitionParser {

		private static final String	COMPILER	= "compiler";
		private static final String	FILLER		= "filler";

		/**
		 * The bean that is created for this tag element.
		 *
		 * @param element
		 *            The tag element
		 * @return A FileListFactoryBean
		 */
		@Override
		protected Class<?> getBeanClass(final Element element) {
			return FileReportStoreEngine.class;
		}

		/**
		 * Called when the fileList tag is to be parsed.
		 *
		 * @param element
		 *            The tag element
		 * @param ctx
		 *            The context in which the parsing is occuring
		 * @param builder
		 *            The bean definitions build to use
		 */
		@Override
		protected void doParse(final Element element, final ParserContext ctx, final BeanDefinitionBuilder builder) {
			Element ebasePath = DomUtils.getChildElementByTagName(element, "base-path");
			List<Element> childElements = DomUtils.getChildElements(ebasePath);
			if (childElements.size() == 0) {
				builder.addPropertyValue("basePath", ebasePath.getNodeValue());
			} else {
//				GenericBeanDefinition parseNode = (GenericBeanDefinition) DefinitionParserUtil.parseNode(childElements.get(0), ctx, builder.getBeanDefinition(),
//				        element.getAttribute("scope"));
//				builder.addPropertyValue("basePathResolver", parseNode);
//				if (!parseNode.getPropertyValues().contains("useMyselfInSpringContext")) {
//					parseNode.getPropertyValues().add("useMyselfInSpringContext", Boolean.TRUE);
//				}
			}
			builder.setDependencyCheck(AbstractBeanDefinition.DEPENDENCY_CHECK_NONE);
			builder.setLazyInit(true);

			// Parse pther childs
			final NodeList childNodes = element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				final Node item = childNodes.item(i);
				if (item instanceof Element) {
					if (ReportEngineParser.COMPILER.equals(item.getLocalName())) {
						this.doParseCompiler(element, ctx, builder, item);
					} else if (ReportEngineParser.FILLER.equals(item.getLocalName())) {
						this.doParseFiller(element, ctx, builder, item);
					}
				}
			}
			builder.setLazyInit(true);
		}

		protected void doParseCompiler(final Element element, final ParserContext ctx, final BeanDefinitionBuilder builder, final Node item) {
			Element child = DomUtils.getChildElements((Element) item).get(0);
			Object compiler = null;
			if ("default-report-compiler".equals(child.getLocalName())) {
				final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(), builder.getBeanDefinition());
				compiler = new ReportCompilerParser().parse(child, nestedCtx);
			} else {
				// construimos el bean que nos venga que deberia ser un IReportCompiler
//				compiler = DefinitionParserUtil.parseNode(child, ctx, builder.getBeanDefinition(), element.getAttribute(ReportBeanDefinitionParser.SCOPE), false);
			}
			builder.addPropertyValue("reportCompiler", compiler);
			builder.setLazyInit(true);
		}

		protected void doParseFiller(final Element element, final ParserContext ctx, final BeanDefinitionBuilder builder, final Node item) {
			Element child = DomUtils.getChildElements((Element) item).get(0);
			Object filler = null;
			if ("default-report-filler".equals(child.getLocalName())) {
				final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(), builder.getBeanDefinition());
				filler = new ReportFillerParser().parse(child, nestedCtx);
			} else {
				// construimos el bean que nos venga que deberia ser un IReportFiller
//				filler = DefinitionParserUtil.parseNode(child, ctx, builder.getBeanDefinition(), element.getAttribute(ReportBeanDefinitionParser.SCOPE), false);
			}
			builder.addPropertyValue("reportFiller", filler);
			builder.setLazyInit(true);
		}
	}

	public static class ReportCompilerParser extends AbstractSingleBeanDefinitionParser {

		/**
		 * The bean that is created for this tag element.
		 *
		 * @param element
		 *            The tag element
		 * @return A FileListFactoryBean
		 */
		@Override
		protected Class<?> getBeanClass(final Element element) {
			return JasperReportsCompilerFiller.class;
		}
	}

	public static class ReportFillerParser extends AbstractSingleBeanDefinitionParser {

		/**
		 * The bean that is created for this tag element.
		 *
		 * @param element
		 *            The tag element
		 * @return A FileListFactoryBean
		 */
		@Override
		protected Class<?> getBeanClass(final Element element) {
			return JasperReportsCompilerFiller.class;
		}
	}
}
