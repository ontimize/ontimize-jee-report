package org.ontimize.jee.report.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.sql.Date;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.services.reportstore.BasicReportDefinition;
import com.ontimize.jee.common.services.reportstore.IReportDefinition;
import com.ontimize.jee.common.services.reportstore.IReportStoreService;
import com.ontimize.jee.common.services.reportstore.ReportOutputType;
import com.ontimize.jee.common.services.reportstore.ReportStoreException;

@RestController
@RequestMapping("/reportstore")
@ComponentScan(basePackageClasses = { com.ontimize.jee.common.services.reportstore.IReportStoreService.class })
public class ReportStoreRestController {

	@Qualifier("ReportStoreService")
	@Autowired
	private IReportStoreService reportStoreService;

	public IReportStoreService getService() {
		return this.reportStoreService;
	}
	
	@RequestMapping(value = "/addReport", method = RequestMethod.POST)
	public EntityResult addReport(
			@RequestParam("file") MultipartFile[] files,
			@RequestParam("data") String data
			) {
		EntityResult res = new EntityResult();
		try {
			HashMap<String, Object> extraData = new HashMap<>();
	        if (data != null) {
	            extraData = new ObjectMapper().readValue(data, HashMap.class);
	        }
	        
	        String id = UUID.randomUUID().toString();
	        String mainReportFilename = files[0].getOriginalFilename().split("\\.")[0] + ".jrxml";
			IReportDefinition rdef = new BasicReportDefinition(id, extraData.get("name").toString(), extraData.get("description").toString(),
					extraData.get("type").toString(), mainReportFilename);
			InputStream reportSource = new ByteArrayInputStream(files[0].getBytes());
			this.reportStoreService.addReport(rdef, reportSource);
			res.setCode(EntityResult.OPERATION_SUCCESSFUL);
		} catch (ReportStoreException e) {
			e.printStackTrace();
			res.setCode(EntityResult.OPERATION_WRONG);
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			res.setCode(EntityResult.OPERATION_WRONG);
			return res;
		}
		
		return res;
	}
	
	@RequestMapping(value = "/fillReport/{id}", method = RequestMethod.POST)
	public EntityResult fillReport(@PathVariable("id") String id,
			@RequestBody (required = true) Map<String, String> bodyParams)
			{
		EntityResult res = new EntityResult();
		ReportOutputType outputType;
		String otherType = "pdf";
		String[] names, values;
		try {
			outputType = ReportOutputType.fromName("pdf");
			Map<String, Object> params = new HashMap<String, Object>();
			if (!bodyParams.get("params").isEmpty()) {
				IReportDefinition reportDefinition = this.reportStoreService.getReportDefinition(id);
				values = bodyParams.get("params").split("\\,");
				for (int i=0; i<reportDefinition.getParameters().size(); i++) {
					params.put(reportDefinition.getParameters().get(i).getName(), this.parseParameter(reportDefinition, i, values[i]));
				}	
			}
			InputStream is = this.reportStoreService.fillReport(id, params, null, outputType, otherType);
			byte[] file = IOUtils.toByteArray(is);
		    is.close();
		    Hashtable<String, Object> map = new Hashtable<String, Object>();
		    map.put("file", file);
		    res.addRecord(map);
			res.setCode(EntityResult.OPERATION_SUCCESSFUL);
		} catch (ReportStoreException e) {
			e.printStackTrace();
			res.setCode(EntityResult.OPERATION_WRONG);
		} catch (IOException e) {
			e.printStackTrace();
			res.setCode(EntityResult.OPERATION_WRONG);
		}		
		return res;
	}
	
	@RequestMapping(value = "/removeReport/{id}", method = RequestMethod.DELETE)
	public EntityResult removeReport(@PathVariable("id") String id) {
		EntityResult res = new EntityResult();
		try {
			this.reportStoreService.removeReport(id);
			res.setCode(EntityResult.OPERATION_SUCCESSFUL);
		} catch (ReportStoreException e) {
			e.printStackTrace();
			res.setCode(EntityResult.OPERATION_WRONG);
			return res;
		}
		return res;
	}
	
	@RequestMapping(value = "/listReports", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public EntityResult listReports() {
		EntityResult res = new EntityResult();
		try {
			Collection<IReportDefinition> reportCollection = this.reportStoreService.listAllReports();
			Object[] reportArray = reportCollection.toArray();
			for (int i=0; i<reportArray.length; i++) {
				BasicReportDefinition reportDefinition = (BasicReportDefinition) reportArray[i];
				Hashtable<String, Object> map = this.fillResponse(reportDefinition);
				res.addRecord(map);
			}
			res.setCode(EntityResult.OPERATION_SUCCESSFUL);
		} catch (ReportStoreException e) {
			e.printStackTrace();
			res.setCode(EntityResult.OPERATION_WRONG);
			return res;
		}
		return res;
	}
	
	@RequestMapping(value = "/getReport/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public EntityResult getReport(@PathVariable("id") String id) {
		EntityResult res = new EntityResult();
		try {
			IReportDefinition reportDefinition = this.reportStoreService.getReportDefinition(id);
			Hashtable<String, Object> map = this.fillResponse(reportDefinition);
			res.addRecord(map);
			res.setCode(EntityResult.OPERATION_SUCCESSFUL);
		} catch (ReportStoreException e) {
			e.printStackTrace();
			res.setCode(EntityResult.OPERATION_WRONG);
			return res;
		}
		return res;
	}
	
	@RequestMapping(value = "/updateReport/{id}", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE})
	public EntityResult updateReport(@PathVariable("id") String id,
			@RequestBody (required = true) Map<String, String> bodyParams) {
		EntityResult res = new EntityResult();
		try {
			Hashtable<String, Object> params = this.fillResponse(this.reportStoreService.getReportDefinition(id));

			if (bodyParams.containsKey("name"))
				params.replace("name", bodyParams.get("name"));
			if (bodyParams.containsKey("description"))
				params.replace("description", bodyParams.get("description"));
			if (bodyParams.containsKey("type"))
				params.replace("type", bodyParams.get("type"));
			if (bodyParams.containsKey("mainReportFilename"))
				params.replace("mainReportFilename", bodyParams.get("mainReportFilename"));
			
			IReportDefinition rdef = new BasicReportDefinition(id, params.get("name").toString(), params.get("description").toString(),
					params.get("type").toString(), params.get("mainReportFilename").toString());
			
			this.reportStoreService.updateReportDefinition(rdef);
			res.setCode(EntityResult.OPERATION_SUCCESSFUL);
		} catch (ReportStoreException e) {
			e.printStackTrace();
			res.setCode(EntityResult.OPERATION_WRONG);
			return res;
		}
		return res;
	}
	
	private Hashtable<String, Object> fillResponse(IReportDefinition reportDefinition){
		Hashtable<String, Object> map = new Hashtable<String, Object>();
		map.put("id", reportDefinition.getId());
		map.put("name", reportDefinition.getName());
		map.put("description", reportDefinition.getDescription());
		map.put("mainReportFilename", reportDefinition.getMainReportFileName());
		map.put("type", reportDefinition.getType());
		map.put("parameters", reportDefinition.getParameters());
		return map;
	}
	
	private Object parseParameter(IReportDefinition rDef, int index, String value) {
		String type = rDef.getParameters().get(index).getValueClass();
		switch (type) {
			case "java.lang.Integer":
				return Integer.parseInt(value);
			case "java.lang.Double":
				return Double.parseDouble(value);
			case "java.lang.Float":
				return Double.parseDouble(value);
			case "java.lang.Real":
				return Float.parseFloat(value);
			case "java.sql.Date":
				return Date.valueOf(value);
			case "java.lang.String":
				break;
		}
		return value;
	}
	
}
