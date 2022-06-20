package com.ontimize.jee.report.server.reportstore;

import com.ontimize.jee.report.common.services.IReportDefinition;
import net.lingala.zip4j.exception.ZipException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The Interface IReportCompiler.
 */
public interface IReportCompiler {

    /**
     * Compile.
     *
     * @param inputZip     the input zip
     * @param outputFolder the output folder
     * @return the compiled zip
     * @throws ZipException
     * @throws IOException
     */
    IReportDefinition compile(Path inputZip, Path outputFolder, IReportDefinition rDef) throws ReportStoreCompileException;
}
