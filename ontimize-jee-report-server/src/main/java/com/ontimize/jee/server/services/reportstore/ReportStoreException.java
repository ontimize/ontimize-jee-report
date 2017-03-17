package com.ontimize.jee.server.services.reportstore;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;

/**
 * The Class ReportStoreException.
 */
public class ReportStoreException extends OntimizeJEEException {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Instantiates a new report store exception.
	 *
	 * @param reason
	 *            the reason
	 */
	public ReportStoreException(String reason) {
		super(reason);
	}

	/**
	 * Instantiates a new report store exception.
	 */
	public ReportStoreException() {
		super();
	}

	/**
	 * Instantiates a new report store exception.
	 *
	 * @param string
	 *            the string
	 * @param parent
	 *            the parent
	 */
	public ReportStoreException(String string, Exception parent) {
		super(string, parent);
	}

	/**
	 * Instantiates a new report store exception.
	 *
	 * @param parent
	 *            the parent
	 */
	public ReportStoreException(Exception parent) {
		super(parent);
	}

}