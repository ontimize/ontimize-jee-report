package com.ontimize.jee.report.common.services;

import com.ontimize.jee.report.common.reportstore.ReportParameter;

import java.util.List;
import java.util.Map;

/**
 * The Interface IReportDefinition.
 */
public interface IReportDefinition {

    /**
     * Gets the reportId.
     *
     * @return the reportId
     */
    Object getReportId();

    /**
     * Gets the reportName.
     *
     * @return the reportName
     */
    String getReportName();

    /**
     * Gets the reportDescription.
     *
     * @return the reportDescription
     */
    String getReportDescription();

    /**
     * Gets the reportType.
     *
     * @return the reporType
     */
    String getReportType();

    /**
     * Gets the other info.
     *
     * @return the other info
     */
    Map<String, String> getOtherInfo();

    /**
     * Gets the main report file name.
     *
     * @return the main report file name
     */
    String getReportFileName();

    /**
     * Gets the report parameters.
     *
     * @return the report parameters
     */
    List<ReportParameter> getParameters();

    /**
     * Sets the report parameters.
     *
     * @return void
     */
    void setParameters(List<ReportParameter> reportParams);

}
