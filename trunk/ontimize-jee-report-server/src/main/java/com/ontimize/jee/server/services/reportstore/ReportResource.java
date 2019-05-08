package com.ontimize.jee.server.services.reportstore;

/**
 * The Interface ReportResource.
 */
public interface ReportResource extends java.io.Serializable {

	/**
	 * Gets the bytes.
	 *
	 * @return the bytes
	 */
	byte[] getBytes();

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	String getName();
}
