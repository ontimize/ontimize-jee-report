package com.ontimize.jee.report.common.exception;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;

/**
 * The Class DynamicReportException.
 */
public class DynamicReportException extends OntimizeJEEException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new dynamic report exception.
	 *
	 * @param reason
	 *            the reason
	 */
	public DynamicReportException(String reason) {
		super(reason);
	}

	/**
	 * Instantiates a new dynamic report exception.
	 */
	public DynamicReportException() {
		super();
	}

	/**
	 * Instantiates a new dynamic report exception.
	 *
	 * @param string
	 *            the string
	 * @param parent
	 *            the parent
	 */
	public DynamicReportException(String string, Exception parent) {
		super(string, parent);
	}

	/**
	 * Instantiates a new dynamic report exception.
	 *
	 * @param parent
	 *            the parent
	 */
	public DynamicReportException(Exception parent) {
		super(parent);
	}

}