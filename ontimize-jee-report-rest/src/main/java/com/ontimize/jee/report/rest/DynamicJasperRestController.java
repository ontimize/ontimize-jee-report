package com.ontimize.jee.report.rest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.services.reportstore.IDynamicJasperService;
import com.ontimize.jee.report.rest.dtos.FunctionParamsDto;
import com.ontimize.jee.report.rest.dtos.ReportParamsDto;

@RestController
@RequestMapping("${ontimize.report.url:/dynamicjasper}")
public class DynamicJasperRestController {

	/** The Constant TOTAL. */
	private static final String TOTAL = "TOTAL";

	@Qualifier("DynamicJasperService")
	@Autowired
	private IDynamicJasperService service;

	public IDynamicJasperService getService() {
		return this.service;
	}

	@RequestMapping(value = "/report", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public EntityResult createReport(@RequestBody ReportParamsDto param) throws Exception {

		EntityResult res = new EntityResultMapImpl();
		InputStream is = service.createReport(param.getColumns(), param.getTitle(), param.getGroups(),
				param.getEntity(), param.getService(), param.getOrientation(), param.getFunctions(),
				param.getStyleFunctions(), param.getSubtitle(), param.getColumnStyle());
		byte[] file = IOUtils.toByteArray(is);
		is.close();
		Hashtable map = new Hashtable<String, Object>();
		map.put("file", file);
		res.addRecord(map);
		return res;
	}

	@RequestMapping(value = "/functionsName", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public EntityResult getFunctionsName(@RequestBody FunctionParamsDto params) throws Exception {
		EntityResult res = new EntityResultMapImpl();
		List<String> list = new ArrayList<>();
		list = service.getFunctionsName(params.getEntity(), params.getService(), params.getColumns());
		list.add(TOTAL);
		Hashtable map = new Hashtable<String, Object>();
		map.put("list", list);
		res.addRecord(map);
		return res;
	}

}
