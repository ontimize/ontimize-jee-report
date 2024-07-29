package com.ontimize.jee.report.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.report.common.dto.ReportStoreParamValueDto;
import com.ontimize.jee.report.common.dto.ReportStoreParamsDto;
import com.ontimize.jee.report.common.exception.ReportStoreException;
import com.ontimize.jee.report.common.reportstore.BasicReportDefinition;
import com.ontimize.jee.report.common.reportstore.ReportOutputType;
import com.ontimize.jee.report.common.reportstore.ReportParameter;
import com.ontimize.jee.report.common.services.IReportDefinition;
import com.ontimize.jee.report.common.services.IReportStoreService;
import com.ontimize.jee.report.common.util.TypeMappingsUtils;
import com.ontimize.jee.server.rest.FilterParameter;
import com.ontimize.jee.server.rest.ORestController;
import com.ontimize.jee.server.rest.ParseUtilsExt;
import com.ontimize.jee.server.rest.QueryParameter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/reportstore")
@ComponentScan(basePackageClasses = {IReportStoreService.class})
public class ReportStoreRestController extends ORestController<IReportStoreService> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportStoreRestController.class);

    @Qualifier("ReportStoreService")
    @Autowired
    private IReportStoreService reportStoreService;

    public IReportStoreService getService() {
        return this.reportStoreService;
    }

    @SuppressWarnings("unchecked")
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
            if(files == null || !(files.length > 0) || files[0] == null ){
                throw new IOException("No report file found");
            }
            MultipartFile multipartFile = files[0];
            String fileName = StringUtils.isEmpty(multipartFile.getOriginalFilename()) ? "reportFile" : multipartFile.getOriginalFilename();
            String mainReportFilename = FilenameUtils.getBaseName(fileName) + ".jrxml";

            IReportDefinition rdef = new BasicReportDefinition(id, extraData.get("name").toString(), extraData.get("description").toString(),
                    extraData.get("type").toString(), mainReportFilename);
            InputStream reportSource = new ByteArrayInputStream(multipartFile.getBytes());

            return this.reportStoreService.addReport(rdef, reportSource);
        } catch (ReportStoreException | IOException e) {
            LOGGER.error("Error adding report", e);
            res.setCode(EntityResult.OPERATION_WRONG);
            res.setMessage("Error adding report! " + e.getMessage());
            return res;
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/fillReport/{id}", method = RequestMethod.POST)
    public EntityResult fillReport(@PathVariable("id") String id,
                                   @RequestBody(required = true) ReportStoreParamsDto bodyParams) {
        EntityResult res = new EntityResultMapImpl();
        ReportOutputType outputType;
        String otherType = "pdf";

        Map<Object, Object> keysValues = new HashMap<Object, Object>();
        try {
            outputType = ReportOutputType.fromName("pdf");
            Map<String, Object> params = processReportParameters(id, bodyParams);
            if (bodyParams.getFilters() != null) {
                keysValues = processFilterParameter(bodyParams).getFilters().getFilter();
            }

            CompletableFuture<EntityResult> future = this.reportStoreService.fillReport(id, params, null, outputType, otherType, keysValues);
            res = future.get();
        } catch (ReportStoreException | InterruptedException | ExecutionException e) {
            LOGGER.error("Error filling report", e);
            if(e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            res.setCode(EntityResult.OPERATION_WRONG);
            res.setMessage("Error filling report! " + e.getMessage());
        }

        return res;
    }

    @RequestMapping(value = "/removeReport/{id}", method = RequestMethod.DELETE)
    public EntityResult removeReport(@PathVariable("id") String id) {
        EntityResult res = new EntityResultMapImpl();
        try {
            return this.reportStoreService.removeReport(id);
        } catch (ReportStoreException e) {
            LOGGER.error("Error removing report", e);
            res.setCode(EntityResult.OPERATION_WRONG);
            res.setMessage("Error removing report! " + e.getMessage());
            return res;
        }
    }

    @RequestMapping(value = "/listReports", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public EntityResult listReports() {
        EntityResult res = new EntityResultMapImpl();
        try {
            return this.reportStoreService.listAllReports();
        } catch (ReportStoreException e) {
            LOGGER.error("Error listing reports", e);
            res.setCode(EntityResult.OPERATION_WRONG);
            res.setMessage("Error listing reports! " + e.getMessage());
            return res;
        }
    }

    @RequestMapping(value = "/getReport/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public EntityResult getReport(@PathVariable("id") String id) {
        EntityResult res = new EntityResultMapImpl();
        try {
            return this.reportStoreService.getReportDefinition(id);
        } catch (ReportStoreException e) {
            LOGGER.error("Error retrieving report", e);
            res.setCode(EntityResult.OPERATION_WRONG);
            res.setMessage("Error retrieving report! " + e.getMessage());
            return res;
        }
    }

    @RequestMapping(value = "/updateReport/{id}", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE})
    public EntityResult updateReport(@PathVariable("id") String id,
                                     @RequestBody(required = true) Map<String, String> bodyParams) {
        EntityResult res = new EntityResultMapImpl();
        try {
            IReportDefinition rDef = this.parseReportEntityResult(this.reportStoreService.getReportDefinition(id));
            Map<String, Object> attr = this.fillResponse(rDef);

            if (bodyParams.containsKey("REPORTNAME"))
                attr.replace("REPORTNAME", bodyParams.get("REPORTNAME"));
            if (bodyParams.containsKey("DESCRIPTION"))
                attr.replace("REPORTDESCRIPTION", bodyParams.get("REPORTDESCRIPTION"));
            if (bodyParams.containsKey("REPORTTYPE"))
                attr.replace("REPORTTYPE", bodyParams.get("REPORTTYPE"));
            if (bodyParams.containsKey("REPORTFILENAME"))
                attr.replace("REPORTFILENAME", bodyParams.get("REPORTFILENAME"));

            IReportDefinition rdef = new BasicReportDefinition(id, attr.get("REPORTNAME").toString(), attr.get("REPORTDESCRIPTION").toString(),
                    attr.get("REPORTTYPE").toString(), attr.get("REPORTFILENAME").toString());

            return this.reportStoreService.updateReportDefinition(rdef);
        } catch (ReportStoreException e) {
            LOGGER.error("Error updating report", e);
            res.setCode(EntityResult.OPERATION_WRONG);
            res.setMessage("Error updating report! " + e.getMessage());
            return res;
        }
    }

    @SuppressWarnings("unchecked")
    private IReportDefinition parseReportEntityResult(EntityResult res) {
        IReportDefinition rDef;
        String uuid, name, description, type, mainReportFilename;

        Map<?, ?> resData = res.getRecordValues(0);
        uuid = (String) resData.get("REPORTUUID");
        name = (String) resData.get("REPORTNAME");
        description = (String) resData.get("REPORTDESCRIPTION");
        type = (String) resData.get("REPORTTYPE");
        mainReportFilename = (String) resData.get("REPORTFILENAME");

        rDef = new BasicReportDefinition(uuid, name, description, type, mainReportFilename);
        rDef.setParameters((List<ReportParameter>) resData.get("PARAMETERS"));

        return rDef;
    }

    private Map<String, Object> fillResponse(IReportDefinition reportDefinition) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("REPORTUUID", reportDefinition.getReportId());
        map.put("REPORTNAME", reportDefinition.getReportName());
        map.put("REPORTDESCRIPTION", reportDefinition.getReportDescription());
        map.put("REPORTFILENAME", reportDefinition.getReportFileName());
        map.put("REPORTTYPE", reportDefinition.getReportType());
        map.put("PARAMETERS", reportDefinition.getParameters());

        return map;
    }

    private Object parseParameter(ReportParameter reportParameter, ReportStoreParamValueDto paramValueDto) {
        Integer sqlType = null;
        if(paramValueDto.getSqlType() != null) {
            sqlType = paramValueDto.getSqlType();
        } else {
            sqlType = TypeMappingsUtils.getSQLTypeFromClassName(reportParameter.getReportParameterValueClass());
        }
        return ParseUtilsExt.getValueForSQLType(paramValueDto.getValue(), sqlType.intValue());
    }

    protected Map<String, Object> processReportParameters(final String id, final ReportStoreParamsDto paramsDto) throws ReportStoreException {
        Map<String, Object> params = new HashMap<String, Object>();
        if (paramsDto.getParameters() != null && !paramsDto.getParameters().isEmpty()) {
            IReportDefinition rDef = this.parseReportEntityResult(this.reportStoreService.getReportDefinition(id));
            List<ReportParameter> reportParameters = rDef.getParameters();
            List<ReportStoreParamValueDto> params1 = paramsDto.getParameters();
            for (int i = 0; i < reportParameters.size(); i++) {
                ReportParameter currentRepParam = reportParameters.get(i);
                ReportStoreParamValueDto paramValueDto = params1.stream().filter(item -> item.getName().equals(currentRepParam.getReportParameterName())).findFirst().orElse(null);
                if(paramValueDto != null) {
                    params.put(currentRepParam.getReportParameterName(), this.parseParameter(currentRepParam, paramValueDto));
                }
            }
        }
        return params;
    }

    protected ReportStoreParamsDto processFilterParameter(final ReportStoreParamsDto param) throws ReportStoreException {
        if (param == null) {
            throw new ReportStoreException("'ReportStoreParams' not found!");
        }

        FilterParameter filterParam = param.getFilters();
        if (filterParam == null) {
            filterParam = new QueryParameter();
        }

        Map<?, ?> kvQueryParameter = filterParam.getFilter();
        List<?> avQueryParameter = filterParam.getColumns();
        Map<?, ?> hSqlTypes = filterParam.getSqltypes();

        Map<Object, Object> processedKeysValues = this.createKeysValues(kvQueryParameter, hSqlTypes);
        List<Object> processedAttributesValues = this.createAttributesValues(avQueryParameter, hSqlTypes);

        filterParam.setKv(processedKeysValues);
        filterParam.setColumns(processedAttributesValues);

        param.setFilters(filterParam);

        return param;
    }

}
