package com.ontimize.jee.report.common.reportstore;

import com.ontimize.jee.report.common.services.IReportDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class BasicReportDefinition.
 */
@SuppressWarnings("serial")
public class BasicReportDefinition implements IReportDefinition, Serializable {

    /**
     * The reportId.
     */
    private Serializable reportId;

    /**
     * The name.
     */
    private String reportName;

    /**
     * The description.
     */
    private String reportDescription;

    /**
     * The type.
     */
    private String reportType;

    /**
     * The main report file name.
     */
    private String reportFileName;

    /**
     * The report parameters.
     */
    private List<ReportParameter> parameters;

    /**
     * The other info.
     */
    private Map<String, String> otherInfo;

    public BasicReportDefinition() {
        super();
    }

    /**
     * Instantiates a new basic report definition.
     *
     * @param id          the id
     * @param name        the name
     * @param description the description
     * @param type        the type
     */
    public BasicReportDefinition(Serializable reportId, String reportName, String reportDescription, String reportType,
                                 String reportFileName) {
        super();
        this.reportId = reportId;
        this.reportName = reportName;
        this.reportDescription = reportDescription;
        this.reportType = reportType;
        this.reportFileName = reportFileName;
        this.parameters = new ArrayList<ReportParameter>();
        this.otherInfo = new HashMap<>();
    }

    public BasicReportDefinition(Serializable reportId, String reportName, String reportDescription, String reportType,
                                 String reportFileName, List<ReportParameter> parameters) {
        super();
        this.reportId = reportId;
        this.reportName = reportName;
        this.reportDescription = reportDescription;
        this.reportType = reportType;
        this.reportFileName = reportFileName;
        this.parameters = parameters;
        this.otherInfo = new HashMap<>();
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public Serializable getReportId() {
        return this.reportId;
    }

    /**
     * Sets the id.
     *
     * @param id the id to set
     */
    public void setReportid(Serializable reportId) {
        this.reportId = reportId;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getReportName() {
        return this.reportName;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    /**
     * Gets the reportDescription.
     *
     * @return the reportDescription
     */
    @Override
    public String getReportDescription() {
        return this.reportDescription;
    }

    /**
     * Sets the reportDescription.
     *
     * @param reportDescription the reportDescription to set
     */
    public void setReportDescription(String reportDescription) {
        this.reportDescription = reportDescription;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    @Override
    public String getReportType() {
        return this.reportType;
    }

    /**
     * Sets the reportType.
     *
     * @param reportType the reportType to set
     */
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.common.services.reportstore.IReportDefinition#getMainReportFileName()
     */
    @Override
    public String getReportFileName() {
        return this.reportFileName;
    }

    /**
     * Sets the report file name.
     *
     * @param mainReportFileName the new report file name
     */
    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }

    /**
     * Gets the other info.
     *
     * @return the otherInfo
     */
    @Override
    public Map<String, String> getOtherInfo() {
        return this.otherInfo;
    }

    /**
     * Sets the other info.
     *
     * @param otherInfo the otherInfo to set
     */
    public void setOtherInfo(Map<String, String> otherInfo) {
        this.otherInfo = otherInfo;
    }

    /**
     * Adds the other info.
     *
     * @param key   the key
     * @param value the value
     */
    public void addOtherInfo(String key, String value) {
        if (this.otherInfo == null) {
            this.otherInfo = new HashMap<>();
        }
        this.otherInfo.put(key, value);
    }

    /**
     * Gets the report parameters.
     *
     * @return the parameters
     */
    @Override
    public List<ReportParameter> getParameters() {
        return parameters;
    }

    /**
     * Sets the main report file name.
     *
     * @param mainReportFileName the new main report file name
     */
    @Override
    public void setParameters(List<ReportParameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "BasicReportDefinition [reportid=" + reportId + ", name=" + reportName + ", description=" + reportDescription + ", type=" + reportType
                + ", reportFilename=" + reportFileName + ", parameters=" + parameters + "]";
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */

}
