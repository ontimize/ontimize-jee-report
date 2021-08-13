package com.ontimize.jee.server.services.reportstore;

import java.io.IOException;
import java.nio.file.Path;

import com.ontimize.jee.common.services.reportstore.IReportDefinition;

import net.lingala.zip4j.exception.ZipException;

/**
 * The Interface IReportCompiler.
 */
public interface IReportCompiler {

	/**
	 * Compile.
	 *
	 * @param inputZip
	 *            the input zip
	 * @param outputFolder
	 *            the output folder
	 * @return the compiled zip
	 * @throws ZipException
	 * @throws IOException
	 */
	IReportDefinition compile(Path inputZip, Path outputFolder, IReportDefinition rDef) throws ReportStoreCompileException;
}
