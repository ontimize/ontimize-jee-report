package com.ontimize.jee.report.rest;

import java.io.InputStream;
import java.util.Hashtable;

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
import com.ontimize.jee.report.rest.dtos.ReportParamsDto;

@RestController
@RequestMapping("/dynamicjasper")
@ComponentScan(basePackageClasses = { com.ontimize.jee.common.services.reportstore.IDynamicJasperService.class })
public class DynamicJasperRestController {

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
				param.getEntity(), param.getService());
		byte[] file = IOUtils.toByteArray(is);
		is.close();
		Hashtable map = new Hashtable<String, Object>();
		map.put("file", file);
		res.addRecord(map);
		return res;
	}

}
