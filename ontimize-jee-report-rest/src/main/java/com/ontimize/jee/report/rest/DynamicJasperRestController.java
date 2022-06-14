package com.ontimize.jee.report.rest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ontimize.jee.report.common.exception.DynamicReportException;
import com.ontimize.jee.server.rest.ORestController;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.report.common.services.IDynamicJasperService;
import com.ontimize.jee.report.common.dto.FunctionParamsDto;
import com.ontimize.jee.report.common.dto.ReportParamsDto;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@RestController
@RequestMapping("${ontimize.report.url:/dynamicjasper}")
public class DynamicJasperRestController {

	/** The Constant TOTAL. */
	private static final String TOTAL = "TOTAL";

	@Qualifier("DynamicJasperService")
	@Autowired
	private IDynamicJasperService service;
	
	@Autowired
	private ApplicationContext applicationContext;

	public IDynamicJasperService getService() {
		return this.service;
	}

	@RequestMapping(value = "/report", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityResult> createReport(@RequestBody ReportParamsDto param) throws Exception {

		String entity = param.getEntity();
		String srv = param.getService();
		String path = param.getPath();


		String[] beanNamesForType = applicationContext.getBeanNamesForType(ORestController.class);
		List<String> restController = Stream.of(beanNamesForType).filter((item) -> item.startsWith(srv.toLowerCase()))
				.collect(Collectors.toList());

		RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext
				.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
		Map<RequestMappingInfo, HandlerMethod> requestMap = requestMappingHandlerMapping.getHandlerMethods();
//		requestMap.forEach((key, value) -> System.out.println(key + " " + value));

		List<HandlerMethod> requestMapHandlerMethodList = requestMap.keySet().stream()
				.filter(key -> key.getActivePatternsCondition().toString().equals("[" + path + "/{name}/search]"))
				.map(requestMap::get)
				.collect(Collectors.toList());
				
		if(requestMapHandlerMethodList.size() == 1){
			Class<?> restControllerBeanName = requestMapHandlerMethodList.get(0).getBeanType();
			Object restControllerBean = applicationContext.getBean(restControllerBeanName);
			if(restControllerBean instanceof ORestController) {
				Object service1 = ((ORestController) restControllerBean).getService();
			}
		}

		
		
		
		

		EntityResult res = new EntityResultMapImpl();
		if(param != null) {
			try {
				InputStream is = service.createReport(param);
				byte[] file = IOUtils.toByteArray(is);
				is.close();
				Map<String,Object> map = new HashMap<>();
				map.put("file", file);
				res.addRecord(map);

				return new ResponseEntity<EntityResult>(res, HttpStatus.OK);
			} catch (DynamicReportException ex) {
				res.setMessage(ex.getMessage());
				res.setCode(EntityResult.OPERATION_WRONG);
				return new ResponseEntity<EntityResult>(res, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			res.setCode(EntityResult.OPERATION_WRONG);
			res.setMessage("Report configuration parameters value is empty.");
			return new ResponseEntity<EntityResult>(res, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/functionsName", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityResult> getFunctionsName(@RequestBody FunctionParamsDto params) throws Exception {
		EntityResult res = new EntityResultMapImpl();
		if(params != null) {
			try {
				List<String> list = service.getFunctionsName(params);
				list.add(TOTAL);
				Map<String,Object> map = new HashMap<>();
				map.put("list", list);
				res.addRecord(map);
				return new ResponseEntity<EntityResult>(res, HttpStatus.OK);
			} catch (DynamicReportException ex) {
				res.setCode(EntityResult.OPERATION_WRONG);
				res.setMessage(ex.getMessage());
				return new ResponseEntity<EntityResult>(res, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			res.setCode(EntityResult.OPERATION_WRONG);
			res.setMessage("Report function parameters value is empty.");
			return new ResponseEntity<EntityResult>(res, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
