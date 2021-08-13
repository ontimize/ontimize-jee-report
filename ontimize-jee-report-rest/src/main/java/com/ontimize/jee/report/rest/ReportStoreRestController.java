package com.ontimize.jee.report.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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
import com.ontimize.jee.common.db.NullValue;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.services.reportstore.BasicReportDefinition;
import com.ontimize.jee.common.services.reportstore.IReportDefinition;
import com.ontimize.jee.common.services.reportstore.IReportStoreService;
import com.ontimize.jee.common.services.reportstore.ReportOutputType;
import com.ontimize.jee.common.services.reportstore.ReportStoreException;
import com.ontimize.jee.server.rest.BasicExpressionProcessor;
import com.ontimize.jee.server.rest.ORestController;
import com.ontimize.jee.server.rest.ParseUtilsExt;


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
		EntityResult res = new EntityResultMapImpl();
		try {
			Map<String, Object> extraData = new HashMap<>();
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
			@RequestBody (required = true) Map<String, Object> bodyParams)
			{
		EntityResult res = new EntityResultMapImpl();
		ReportOutputType outputType;
		String otherType = "pdf";
		String[] values;
		Map<Object, Object> filter = ((Map<Object, Object>) bodyParams.get("filter"));
		Map<Object, Object> keysValues = new HashMap<Object, Object>();
		try {
			outputType = ReportOutputType.fromName("pdf");
			Map<String, Object> params = new HashMap<String, Object>();
			if (!((String) bodyParams.get("params")).isEmpty()) {
				IReportDefinition reportDefinition = this.reportStoreService.getReportDefinition(id);
				values = ((String) bodyParams.get("params")).split("\\,");
				for (int i=0; i<reportDefinition.getParameters().size(); i++) {
					params.put(reportDefinition.getParameters().get(i).getName(), this.parseParameter(reportDefinition, i, values[i]));
				}	
			}
			if (!filter.isEmpty()) {
				filter = (Map<Object, Object>) filter.get("filter");
				keysValues = this.createKeysValues(filter, new HashMap<>());
			}
			
			InputStream is = this.reportStoreService.fillReport(id, params, null, outputType, otherType, keysValues);
			byte[] file = IOUtils.toByteArray(is);
		    is.close();
		    Map<String, Object> map = new HashMap<String, Object>();
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
		EntityResult res = new EntityResultMapImpl();
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
		EntityResult res = new EntityResultMapImpl();
		try {
			Collection<IReportDefinition> reportCollection = this.reportStoreService.listAllReports();
			Object[] reportArray = reportCollection.toArray();
			for (int i=0; i<reportArray.length; i++) {
				BasicReportDefinition reportDefinition = (BasicReportDefinition) reportArray[i];
				Map <String, Object> map = this.fillResponse(reportDefinition);
				res.addRecord(map);
//				res.putAll(map);
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
		EntityResult res = new EntityResultMapImpl();
		try {
			IReportDefinition reportDefinition = this.reportStoreService.getReportDefinition(id);
			Map<String, Object> map = this.fillResponse(reportDefinition);
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
		EntityResult res = new EntityResultMapImpl();
		try {
			Map<String, Object> params = this.fillResponse(this.reportStoreService.getReportDefinition(id));

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
	
	private Map<String, Object> fillResponse(IReportDefinition reportDefinition){
		Map<String, Object> map = new HashMap<String, Object>();
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
			case "java.sql.Timestamp":
				return Timestamp.valueOf(value);
			case "java.lang.String":
				break;
		}
		return value;
	}
	
	protected Map<Object, Object> createKeysValues(Map<?, ?> kvQueryParam, Map<?, ?> hSqlTypes) {
        Map<Object, Object> kv = new HashMap<>();
        if ((kvQueryParam == null) || kvQueryParam.isEmpty()) {
            return kv;
        }

        if (kvQueryParam.containsKey(ORestController.BASIC_EXPRESSION)) {
            Object basicExpressionValue = kvQueryParam.remove(ORestController.BASIC_EXPRESSION);
            this.processBasicExpression(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY, kv,
                    basicExpressionValue, hSqlTypes);
        }

        if (kvQueryParam.containsKey(ORestController.FILTER_EXPRESSION)) {
            Object basicExpressionValue = kvQueryParam.remove(ORestController.FILTER_EXPRESSION);
            this.processBasicExpression(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.FILTER_KEY, kv,
                    basicExpressionValue, hSqlTypes);
        }

        for (Entry<?, ?> next : kvQueryParam.entrySet()) {
            Object key = next.getKey();
            Object value = next.getValue();
            if ((hSqlTypes != null) && hSqlTypes.containsKey(key)) {
                int sqlType = (Integer) hSqlTypes.get(key);
                value = ParseUtilsExt.getValueForSQLType(value, sqlType);
                if (value == null) {
                    if (ParseUtilsExt.BASE64 == sqlType) {
                        sqlType = Types.BINARY;
                    }
                    value = new NullValue(sqlType);
                }
            } else if (value == null) {
                value = new NullValue();
            }
            kv.put(key, value);
        }
        return kv;
    }
	
	protected void processBasicExpression(String key, Map<?, ?> keysValues, Object basicExpression,
            Map<?, ?> hSqlTypes) {
        if (basicExpression instanceof Map) {
            try {
                BasicExpression bE = BasicExpressionProcessor.getInstance()
                    .processBasicEspression(basicExpression, hSqlTypes);
                ((Map<Object, Object>) keysValues).put(key, bE);
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }

    }

    protected void processBasicExpression(String key, Map<Object, Object> keysValues, Object basicExpression) {
        this.processBasicExpression(key, keysValues, basicExpression, new HashMap<>());
    }
	
}
