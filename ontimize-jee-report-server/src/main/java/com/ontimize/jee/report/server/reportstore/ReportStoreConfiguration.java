package com.ontimize.jee.report.server.reportstore;

import com.ontimize.jee.common.tools.CheckingTools;

/**
 * The Class RemotePreferencesConfiguration.
 */
public class ReportStoreConfiguration {

    /**
     * The engine.
     */
    private IReportStoreEngine engine;

    /**
     * Gets the engine.
     *
     * @return the engine
     */
    public IReportStoreEngine getEngine() {
        CheckingTools.failIfNull(this.engine, "No report store engine defined");
        return this.engine;
    }

    /**
     * Sets the engine.
     *
     * @param engine the engine
     */
    public void setEngine(IReportStoreEngine engine) {
        this.engine = engine;
    }

}
