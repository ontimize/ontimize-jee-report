package com.ontimize.jee.common.services.reportstore;

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

}
