package com.ontimize.jee.report.server.reportstore;

import com.ontimize.jee.report.common.exception.ReportStoreException;

@SuppressWarnings("serial")
public class ReportStoreCompileException extends ReportStoreException {

	public ReportStoreCompileException(String reason) {
		super(reason);
	}

	public ReportStoreCompileException() {
		super();
	}

	public ReportStoreCompileException(String string, Exception parent) {
		super(string, parent);
	}

	public ReportStoreCompileException(Exception parent) {
		super(parent);
	}

}
