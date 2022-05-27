package com.ontimize.jee.report.server.reportstore;

import com.ontimize.jee.report.common.services.IReportStoreService;

/**
 * The Interface IReportStoreService.
 */
public interface IReportStoreServiceServer extends IReportStoreService {

	/**
	 * Update settings.
	 */
	void updateSettings();

}