package com.ontimize.jee.server.services.reportstore;

import com.ontimize.jee.common.services.reportstore.ReportStoreException;

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
