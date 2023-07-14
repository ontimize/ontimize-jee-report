package com.ontimize.jee.report.rest;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.report.common.dto.FunctionParamsDto;
import com.ontimize.jee.report.common.dto.FunctionTypeDto;
import com.ontimize.jee.report.common.dto.ReportParamsDto;
import com.ontimize.jee.report.common.exception.DynamicReportException;
import com.ontimize.jee.report.common.services.IDynamicJasperService;
import com.ontimize.jee.server.rest.FilterParameter;
import com.ontimize.jee.server.rest.ORestController;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${ontimize.report.url:/dynamicjasper}")
public class DynamicJasperRestController extends ORestController {

    /**
     * The Constant TOTAL.
     */
    private static final String TOTAL = "TOTAL";

    @Qualifier("DynamicJasperService")
    @Autowired
    private IDynamicJasperService service;

    public IDynamicJasperService getService() {
        return this.service;
    }

    @RequestMapping(value = "/report", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> createReport(@RequestBody ReportParamsDto param) throws Exception {

        EntityResult res = new EntityResultMapImpl();
        if (param != null) {
            try {

                InputStream is = service.createReport(processFilterParameter(param));
                byte[] file = IOUtils.toByteArray(is);
                is.close();
                Map<String, Object> map = new HashMap<>();
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
        if (params != null) {
            try {
                List<FunctionTypeDto> list = service.getFunctionsName(params);
                Map<String, Object> map = new HashMap<>();
                map.put("functions", list);
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

    protected ReportParamsDto processFilterParameter(final ReportParamsDto param) {
        FilterParameter filterParam = param.getFilters();
        Map<?, ?> kvQueryParameter = filterParam.getFilter();
        List<?> avQueryParameter = filterParam.getColumns();
        HashMap<?, ?> hSqlTypes = filterParam.getSqltypes();

        Map<Object, Object> processedKeysValues = this.createKeysValues(kvQueryParameter, hSqlTypes);
        List<Object> processedAttributesValues = this.createAttributesValues(avQueryParameter, hSqlTypes);
        filterParam.setKv(processedKeysValues);
        filterParam.setColumns(processedAttributesValues);
        param.setFilters(filterParam);
        return param;

    }
}
