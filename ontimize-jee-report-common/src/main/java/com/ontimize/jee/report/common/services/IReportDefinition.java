package com.ontimize.jee.report.common.services;

import com.ontimize.jee.common.util.remote.BytesBlock;
import com.ontimize.jee.report.common.reportstore.ReportParameter;

import java.util.List;
import java.util.Map;

/**
 * The Interface IReportDefinition.
 */
public interface IReportDefinition {

    /**
     * Gets the id.
     *
     * @return the id
     */
    Object getId();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the description.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the report type.
     *
     * @return the report type
     */
    String getType();

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
    String getMainReportFileName();

    /**
     * Gets the report parameters.
     *
     * @return the report parameters
     */
    List<ReportParameter> getParameters();
    
    BytesBlock getZip();

    /**
     * Sets the report parameters.
     *
     * @return void
     */
    void setParameters(List<ReportParameter> reportParams);

}
