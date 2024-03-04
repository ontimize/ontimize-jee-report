package com.ontimize.jee.report.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.util.remote.BytesBlock;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/reportstore")
@ComponentScan(basePackageClasses = { IReportStoreService.class })
public class ReportStoreRestController extends ORestController<IReportStoreService> {

    @Qualifier("ReportStoreService")
    @Autowired
    private IReportStoreService reportStoreService;

    public IReportStoreService getService() {
        return this.reportStoreService;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/addReport", method = RequestMethod.POST)
    public EntityResult addReport(@RequestParam("file") MultipartFile[] files, @RequestParam("data") String data) {
        EntityResult res = new EntityResultMapImpl();
        try {
            Map<String, Object> extraData = new HashMap<>();
            if (data != null) {
                extraData = new ObjectMapper().readValue(data, HashMap.class);
            }

            String id = UUID.randomUUID().toString();
            String mainReportFilename = files[0].getOriginalFilename().split("\\.")[0] + ".jrxml";

            IReportDefinition rdef = new BasicReportDefinition(id, extraData.get("name").toString(),
                    extraData.get("description").toString(), extraData.get("type").toString(), mainReportFilename);
            InputStream reportSource = new ByteArrayInputStream(files[0].getBytes());

            return this.reportStoreService.addReport(rdef, reportSource);
        } catch (ReportStoreException e) {
            e.printStackTrace();
            res.setCode(EntityResult.OPERATION_WRONG);
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            res.setCode(EntityResult.OPERATION_WRONG);
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

            CompletableFuture<EntityResult> future = this.reportStoreService.fillReport(id, params, null, outputType,
                    otherType, keysValues);
            res = future.get();
        } catch (ReportStoreException e) {
            e.printStackTrace();
            res.setMessage(e.getMessage());
            res.setCode(EntityResult.OPERATION_WRONG);
        } catch (InterruptedException e) {
            e.printStackTrace();
            res.setMessage(e.getMessage());
            res.setCode(EntityResult.OPERATION_WRONG);
        } catch (ExecutionException e) {
            e.printStackTrace();
            res.setMessage(e.getMessage());
            res.setCode(EntityResult.OPERATION_WRONG);
        }

        return res;
    }

    @RequestMapping(value = "/removeReport/{id}", method = RequestMethod.DELETE)
    public EntityResult removeReport(@PathVariable("id") String id) {
        EntityResult res = new EntityResultMapImpl();
        try {
            return this.reportStoreService.removeReport(id);
        } catch (ReportStoreException e) {
            e.printStackTrace();
            res.setCode(EntityResult.OPERATION_WRONG);
            return res;
        }
    }

    @RequestMapping(value = "/listReports", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public EntityResult listReports() {
        EntityResult res = new EntityResultMapImpl();
        try {
            return this.reportStoreService.listAllReports();
        } catch (ReportStoreException e) {
            e.printStackTrace();
            res.setCode(EntityResult.OPERATION_WRONG);
            return res;
        }
    }

    @RequestMapping(value = "/getReport/{id}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public EntityResult getReport(@PathVariable("id") String id) {
        EntityResult res = new EntityResultMapImpl();
        try {
            return this.reportStoreService.getReportDefinition(id);
        } catch (ReportStoreException e) {
            e.printStackTrace();
            res.setCode(EntityResult.OPERATION_WRONG);
            return res;
        }
    }

    public String getJrxmlFileNameFromZip(InputStream zipInputStream) {
        String jrxmlFileName = null; 
        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".jrxml")) {
                    jrxmlFileName = entry.getName();
                    break; 
                }
                zis.closeEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jrxmlFileName;
    }

    @RequestMapping(value = "/updateReport/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityResult updateReport(@PathVariable("id") String id, @RequestBody(required = true) Map<String, Object> bodyParams) throws IOException {
        EntityResult res = new EntityResultMapImpl();
        try {
            IReportDefinition rDef = this.parseReportEntityResult(this.reportStoreService.getReportDefinition(id));
            Map<String, Object> attr = this.fillResponse(rDef);

            // Actualizar atributos simples
            if (bodyParams.containsKey("NAME"))
                attr.replace("NAME", bodyParams.get("NAME").toString());
            if (bodyParams.containsKey("DESCRIPTION"))
                attr.replace("DESCRIPTION", bodyParams.get("DESCRIPTION").toString());
            if (bodyParams.containsKey("REPORT_TYPE"))
                attr.replace("REPORT_TYPE", bodyParams.get("REPORT_TYPE").toString());
            if (bodyParams.containsKey("MAIN_REPORT_FILENAME"))
                attr.replace("MAIN_REPORT_FILENAME", bodyParams.get("MAIN_REPORT_FILENAME").toString());

            InputStream fileInputStream = null;
            // Manejo especial para FILES
            if (bodyParams.containsKey("FILES")) {
               fileInputStream=decodeBase64ToInputStream(bodyParams.get("FILES").toString());
            }

            IReportDefinition rdef = new BasicReportDefinition(id, attr.get("NAME").toString(),
                    attr.get("DESCRIPTION").toString(), attr.get("REPORT_TYPE").toString(),
                    getJrxmlFileNameFromZip(fileInputStream));

            // Pasar fileInputStream en lugar de null
            return this.reportStoreService.updateReportDefinition(rdef, fileInputStream);
        } catch (ReportStoreException e) {
            e.printStackTrace();
            res.setCode(EntityResult.OPERATION_WRONG);
            return res;
        }
    }
    public static InputStream decodeBase64ToInputStream(String base64String) {
        
        String encodedFile = base64String.substring(base64String.indexOf(",") + 1);

        // Decodificar la cadena Base64 a un array de bytes
        byte[] decodedBytes = Base64.getDecoder().decode(encodedFile);

        // Convertir el array de bytes en un InputStream
        InputStream inputStream = new ByteArrayInputStream(decodedBytes);

        return inputStream;
    }


    @SuppressWarnings("unchecked")
    private IReportDefinition parseReportEntityResult(EntityResult res) {
        IReportDefinition rDef;
        String uuid, name, description, type, mainReportFilename;

        Map<?, ?> resData = res.getRecordValues(0);
        uuid = (String) resData.get("UUID");
        name = (String) resData.get("NAME");
        description = (String) resData.get("DESCRIPTION");
        type = (String) resData.get("REPORT_TYPE");
        mainReportFilename = (String) resData.get("MAIN_REPORT_FILENAME");

        rDef = new BasicReportDefinition(uuid, name, description, type, mainReportFilename);
        rDef.setParameters((List<ReportParameter>) resData.get("PARAMETERS"));

        return rDef;
    }

    private Map<String, Object> fillResponse(IReportDefinition reportDefinition) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("UUID", reportDefinition.getId());
        map.put("NAME", reportDefinition.getName());
        map.put("DESCRIPTION", reportDefinition.getDescription());
        map.put("MAIN_REPORT_FILENAME", reportDefinition.getMainReportFileName());
        map.put("REPORT_TYPE", reportDefinition.getType());
        map.put("PARAMETERS", reportDefinition.getParameters());

        return map;
    }

    private Object parseParameter(ReportParameter reportParameter, ReportStoreParamValueDto paramValueDto) {
        Integer sqlType = null;
        if (paramValueDto.getSqlType() != null) {
            sqlType = paramValueDto.getSqlType();
        } else {
            sqlType = TypeMappingsUtils.getSQLTypeFromClassName(reportParameter.getValueClass());
        }
        return ParseUtilsExt.getValueForSQLType(paramValueDto.getValue(), sqlType.intValue());
    }

    protected Map<String, Object> processReportParameters(final String id, final ReportStoreParamsDto paramsDto)
            throws ReportStoreException {
        Map<String, Object> params = new HashMap<String, Object>();
        if (paramsDto.getParameters() != null && !paramsDto.getParameters().isEmpty()) {
            IReportDefinition rDef = this.parseReportEntityResult(this.reportStoreService.getReportDefinition(id));
            List<ReportParameter> reportParameters = rDef.getParameters();
            List<ReportStoreParamValueDto> params1 = paramsDto.getParameters();
            for (int i = 0; i < reportParameters.size(); i++) {
                ReportParameter currentRepParam = reportParameters.get(i);
                ReportStoreParamValueDto paramValueDto = params1.stream()
                        .filter(item -> item.getName().equals(currentRepParam.getName())).findFirst().orElse(null);
                if (paramValueDto != null) {
                    params.put(currentRepParam.getName(), this.parseParameter(currentRepParam, paramValueDto));
                }
            }
        }
        return params;
    }

    protected ReportStoreParamsDto processFilterParameter(final ReportStoreParamsDto param)
            throws ReportStoreException {
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
