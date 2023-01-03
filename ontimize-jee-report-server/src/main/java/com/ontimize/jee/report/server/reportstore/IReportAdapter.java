package com.ontimize.jee.report.server.reportstore;

import com.ontimize.jee.report.common.services.IReportDefinition;

import java.util.Map;

/**
 * The Interface IReportService.
 */
public interface IReportAdapter {

    /**
     * Before report begins.
     *
     * @param definition the definition
     * @param parameters the parameters
     */
    void beforeReportBegins(IReportDefinition definition, Map<String, Object> parameters);

    /**
     * After report ends.
     *
     * @param definition the definition
     * @param parameters the parameters
     */
    void afterReportEnds(IReportDefinition definition, Map<String, Object> parameters);

    /**
     * On report error.
     *
     * @param definition the definition
     * @param parameters the parameters
     * @param ex         the ex
     */
    void onReportError(IReportDefinition definition, Map<String, Object> parameters, Exception ex);

}
