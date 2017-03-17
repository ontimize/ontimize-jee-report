package com.ontimize.jee.common.services.reportstore;

public enum ReportOutputType {
	PDF("pdf"), JASPER_REPORT("jasperprint", null), DOCX("docx"), XLSX("xlsx"), HTML("html"), OTHER("other", null);

	private String name;

	private String	extension;

	private ReportOutputType(String name){
		this(name, name);
	}

	private ReportOutputType(String name, String extension) {
		this.name = name;
		this.extension = extension;
	}

	public String getName() {
		return this.name;
	}

	public String getExtension() {
		return this.extension;
	}

	public static ReportOutputType fromName(String name) throws ReportStoreException {
		switch (name) {
			case "pdf":
				return PDF;
			case "jasperprint":
				return JASPER_REPORT;
			case "docx":
				return DOCX;
			case "xlsx":
				return XLSX;
			case "html":
				return HTML;
			case "other":
				return OTHER;
			default:
				throw new ReportStoreException("E_INVALID_REPORT_OUTPUT_TYPE");
		}
	}
}
