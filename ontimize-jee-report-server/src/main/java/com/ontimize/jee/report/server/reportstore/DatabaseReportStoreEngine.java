package com.ontimize.jee.report.server.reportstore;

import com.ontimize.jee.common.db.SQLStatementBuilder.SQLOrder;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.common.tools.PathTools;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.common.util.remote.BytesBlock;
import com.ontimize.jee.report.common.exception.ReportStoreException;
import com.ontimize.jee.report.common.reportstore.BasicReportDefinition;
import com.ontimize.jee.report.common.reportstore.ReportOutputType;
import com.ontimize.jee.report.common.reportstore.ReportParameter;
import com.ontimize.jee.report.common.services.IReportDefinition;
import com.ontimize.jee.report.common.util.AdvancedEntityResultDataSource;
import com.ontimize.jee.report.common.util.EntityResultDataSource;
import com.ontimize.jee.report.server.dao.IReportDao;
import com.ontimize.jee.report.server.dao.IReportParameterDao;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import com.ontimize.jee.server.dao.common.INameConvention;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class DatabaseReportStoreEngine implements IReportStoreEngine, ApplicationContextAware, InitializingBean {

    /**
     * The Constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(DatabaseReportStoreEngine.class);

//	/** The Constant ERROR_UPDATING_REPORT. */
//	private static final String					ERROR_UPDATING_REPORT			= "E_UPDATING_REPORT";

    /**
     * The Constant ERROR_ADDING_REPORT.
     */
    private static final String ERROR_ADDING_REPORT = "E_ADDING_REPORT";

    /**
     * The Constant ERROR_NO_REPORT_DEFINITION.
     */
    private static final String ERROR_NO_REPORT_DEFINITION = "E_NO_REPORT_DEFINITION";

    /**
     * The Constant ERROR_GETTING_REPORT_SOURCE.
     */
    private static final String ERROR_GETTING_REPORT_SOURCE = "E_GETTING_REPORT_SOURCE";

    /**
     * The Constant ERROR_REPORT_ID_NOT_EXISTS.
     */
    private static final String ERROR_REPORT_ID_NOT_EXISTS = "E_REPORT_ID_NOT_EXISTS";

    /**
     * The Constant ERROR_NO_REPORT_KEY.
     */
    private static final String ERROR_NO_REPORT_KEY = "E_NO_REPORT_KEY";

    /**
     * The Constant ZIP_EXTENSION.
     */
    private static final String ZIP_EXTENSION = ".zip";

    /**
     * The Constant PREFIX.
     */
    private static final String PREFIX = "rep";

    /**
     * The Constant JASPER.
     */
    private static final String JASPER = ".jasper";

    /**
     * The bundle.
     */
    private ResourceBundle bundle;

    /**
     * The report compiler.
     */
    private IReportCompiler reportCompiler;

    /**
     * The report filler.
     */
    private IReportFiller reportFiller;

    /**
     * The application context.
     */
    private ApplicationContext applicationContext;

    /**
     * The Ontimize DAO helper.
     */
    @Autowired
    private DefaultOntimizeDaoHelper daoHelper;

    /**
     * The report entity DAO.
     */
    @Autowired
    private IReportDao reportDao;

    /**
     * The report parameter entity DAO.
     */
    @Autowired
    private IReportParameterDao reportParameterDao;

    /**
     * Instantiates a new file report store.
     */

    @Autowired
    private INameConvention nameConvention;

    public DatabaseReportStoreEngine() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#addReport(com.ontimize.jee.common.services.reportstore.IReportDefinition, java.io.InputStream)
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public EntityResult addReport(IReportDefinition rDef, InputStream reportSource) throws ReportStoreException {
//        Path reportFolder = null;
        try {
            CheckingTools.failIfNull(rDef, DatabaseReportStoreEngine.ERROR_NO_REPORT_DEFINITION);
            CheckingTools.failIfNull(rDef.getId(), DatabaseReportStoreEngine.ERROR_NO_REPORT_KEY);

            Map<String, Object> attrMap = this.parseReportAttributes(rDef);
            byte[] zip = IOUtils.toByteArray(reportSource);
            attrMap.put(this.nameConvention.convertName("ZIP"), zip);
            EntityResult res = this.daoHelper.insert(this.reportDao, attrMap);
            this.convertToUpperColumnsEntityResult(res);
            this.compileReport(rDef.getId(), rDef);
            return res;
        } catch (ReportStoreException error) {
//            PathTools.deleteFolderSafe(reportFolder);
            throw error;
        } catch (Exception ex) {
            try {
//                PathTools.deleteFolderSafe(reportFolder);
            } catch (Exception ex2) {
                logger.warn("", ex2);
            }
            throw new ReportStoreException(DatabaseReportStoreEngine.ERROR_ADDING_REPORT, ex);
        }
    }

    private void safeCreateDirectories(Path dirToCreate) throws IOException {
        try {
            Files.createDirectories(dirToCreate);
        } catch (IOException ex) {
            // Some error in recursive directories creation, try to create one by one
            DatabaseReportStoreEngine.logger.warn("E_BUILDING_BASE_DIRECTORIES__TRYING_AGAIN_ONE_BY_ONE", ex);
            List<Path> pathsInOrder = this.getPathsHierarchy(dirToCreate);
            for (Path path : pathsInOrder) {
                if (!Files.exists(path)) {
                    try {
                        Files.createDirectory(path);
                    } catch (IOException ex2) {
                        DatabaseReportStoreEngine.logger.warn("E_BUILDING_BASE_DIRECTORY__" + path, ex2);
                    }
                } else if (!Files.isDirectory(path)) {
                    throw new IOException("E_INVALID_DIRECTORY__" + path);
                }
            }
        }
        if (!Files.exists(dirToCreate) || !Files.isDirectory(dirToCreate)) {
            throw new IOException("E_BUILDING_DIRECTORY__" + dirToCreate);
        }
    }

    private List<Path> getPathsHierarchy(Path basePath) {
        List<Path> paths = new ArrayList<>();
        Path current = basePath;
        while (current != null) {
            paths.add(0, current);
            current = current.getParent();
        }
        return paths;
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#updateReportDefinition(com.ontimize.jee.common.services.reportstore. IReportDefinition)
     */
    @Transactional
    @Override
    public EntityResult updateReportDefinition(IReportDefinition rDef) throws ReportStoreException {
        CheckingTools.failIfNull(rDef, DatabaseReportStoreEngine.ERROR_NO_REPORT_DEFINITION);
        CheckingTools.failIfNull(rDef.getId(), DatabaseReportStoreEngine.ERROR_NO_REPORT_KEY);

        Map<String, Object> keyMap = new HashMap<>();
        List<String> attrList = new ArrayList<>();

        keyMap.put(this.nameConvention.convertName("UUID"), rDef.getId());
        attrList.add(this.nameConvention.convertName("ID"));
        EntityResult res = this.daoHelper.query(this.reportDao, keyMap, attrList);
        this.convertToUpperColumnsEntityResult(res);
        Map<?, ?> resData = res.getRecordValues(0);
        Integer id = (Integer) resData.get(this.nameConvention.convertName("ID"));

        keyMap.clear();
        keyMap.put(this.nameConvention.convertName("ID"), id);
        Map<String, Object> attrValues = new HashMap<>();
        attrValues = this.parseReportAttributes(rDef);
        return this.daoHelper.update(this.reportDao, attrValues, keyMap);
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#removeReport(java.lang.Object)
     */
    @Override
    public EntityResult removeReport(Object reportId) throws ReportStoreException {
        CheckingTools.failIfNull(reportId, DatabaseReportStoreEngine.ERROR_NO_REPORT_KEY);

        Map<String, Object> keyMap = new HashMap<>();
        List<String> attrList = new ArrayList<>();

        keyMap.put(this.nameConvention.convertName("UUID"), reportId);
        attrList.add(this.nameConvention.convertName("ID"));
        EntityResult res = this.daoHelper.query(this.reportDao, keyMap, attrList);
        this.convertToUpperColumnsEntityResult(res);
        Map<?, ?> resData = res.getRecordValues(0);
        Integer id = (Integer) resData.get(this.nameConvention.convertName("ID"));

        // Check if there's parameters for this reportId (FK restriction)
        keyMap.clear();
        keyMap.put(this.nameConvention.convertName("REPORT_ID"), id);
        res = this.daoHelper.query(this.reportParameterDao, keyMap, attrList);
        this.convertToUpperColumnsEntityResult(res);
        if (!res.entrySet().isEmpty()) {
            int size = res.calculateRecordNumber();
            for (int i = 0; i < size; i++) {
                resData = res.getRecordValues(i);
                Integer paramId = (Integer) resData.get(this.nameConvention.convertName("ID"));
                keyMap.clear();
                keyMap.put(this.nameConvention.convertName("ID"), paramId);
                this.daoHelper.delete(this.reportParameterDao, keyMap);
            }
        }

        // Remove the report
        keyMap.clear();
        keyMap.put(this.nameConvention.convertName("ID"), id);
        res = this.daoHelper.delete(this.reportDao, keyMap);
        PathTools.deleteFolderSafe(this.getReportFolder(reportId));
        return res;
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#getReportDefinition(java.lang.Object)
     */
    @Override
    public EntityResult getReportDefinition(Object reportId) throws ReportStoreException {
        CheckingTools.failIfNull(reportId, DatabaseReportStoreEngine.ERROR_NO_REPORT_KEY);

        Map<String, Object> keyMap = new HashMap<>();
        List<String> attrList = new ArrayList<>();

        // Retrieve report data
        attrList.add(this.nameConvention.convertName("ID"));
        attrList.add(this.nameConvention.convertName("UUID"));
        attrList.add(this.nameConvention.convertName("NAME"));
        attrList.add(this.nameConvention.convertName("DESCRIPTION"));
        attrList.add(this.nameConvention.convertName("REPORT_TYPE"));
        attrList.add(this.nameConvention.convertName("MAIN_REPORT_FILENAME"));
        keyMap.put(this.nameConvention.convertName("UUID"), reportId);
        EntityResult res = this.daoHelper.query(this.reportDao, keyMap, attrList);
        this.convertToUpperColumnsEntityResult(res);
        IReportDefinition rDef = this.parseReportEntityResult(res);

        // Retrieve report parameters data
        Map<?, ?> resData = res.getRecordValues(0);
        Integer id = (Integer) resData.get("ID");
        attrList.clear();
        keyMap.clear();
        attrList.add(this.nameConvention.convertName("NAME"));
        attrList.add(this.nameConvention.convertName("DESCRIPTION"));
        attrList.add(this.nameConvention.convertName("NESTED_TYPE"));
        attrList.add(this.nameConvention.convertName("VALUE_CLASS"));
        keyMap.put(this.nameConvention.convertName("REPORT_ID"), id);
        res = this.daoHelper.query(this.reportParameterDao, keyMap, attrList);
        this.convertToUpperColumnsEntityResult(res);
        if (res.calculateRecordNumber() == 0 ){
            this.convertToUpperSQLTypesEntityResult(res);
        }
        if (!res.entrySet().isEmpty()) {
            List<ReportParameter> params = this.parseReportParameterEntityResult(res);
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i).getName().equalsIgnoreCase("service")
                        || params.get(i).getName().equalsIgnoreCase("entity")
                        || params.get(i).getName().equalsIgnoreCase("pagesize")) {
                    params.remove(i);
                    i--;
                }
            }
            rDef.setParameters(params);
        }

        res.clear();
        res.addRecord(this.fillResponse(rDef));
        return res;
    }


    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#listAllReports()
     */
    @Override
    public EntityResult listAllReports() throws ReportStoreException {
        List<String> attrList = new ArrayList<>();
        attrList.add(this.nameConvention.convertName("UUID"));
        attrList.add(this.nameConvention.convertName("NAME"));
        attrList.add(this.nameConvention.convertName("DESCRIPTION"));
        attrList.add(this.nameConvention.convertName("REPORT_TYPE"));
        attrList.add(this.nameConvention.convertName("MAIN_REPORT_FILENAME"));
        Map<String, Object> keyMap = new HashMap<>();

        EntityResult res = this.daoHelper.query(this.reportDao, keyMap, attrList);
        convertToUpperColumnsEntityResult(res);
        return res;
    }

    public void convertToUpperColumnsEntityResult(EntityResult res){

        List<String> columns = new ArrayList<>(res.keySet());
        for (String column : columns) {
            EntityResultTools.renameColumn(res, column, column.toUpperCase());
        }
    }

    public void convertToUpperSQLTypesEntityResult(EntityResult res){

        List<String> columns = new ArrayList<>(res.getColumnSQLTypes().keySet());

        for (String column : columns) {
            Object typeToRename = res.getColumnSQLTypes().remove(column);
            if (typeToRename != null){
                res.getColumnSQLTypes().put(column.toUpperCase(),typeToRename);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#fillReport(java.lang.Object, java.util.Map,
     * com.ontimize.jee.server.services.reportstore.ReportOutputType, java.lang.String)
     */
    @Override
    public CompletableFuture<EntityResult> fillReport(Object reportId, Map<String, Object> reportParameters, String dataSourceName, ReportOutputType outputType,
                                                      String otherType, Map<Object, Object> keysValues) throws ReportStoreException {
        String service = null;
        String entity = null;
        Integer index;
        Integer pagesize = null;
        EntityResult entityResult;
        InputStream is = null;

        try {
            // Retrieve the compiled report from the database (REPORT.COMPILED)
            Map<String, Object> keyMap = new HashMap<String, Object>();
            keyMap.put(this.nameConvention.convertName("UUID"), reportId);
            List<String> attrList = new ArrayList<>();
            attrList.add(this.nameConvention.convertName("COMPILED"));
            EntityResult res = this.daoHelper.query(this.reportDao, keyMap, attrList);
            this.convertToUpperColumnsEntityResult(res);
            Map<?, ?> resData = res.getRecordValues(0);

            // Parse the byte array to JasperReport object

            byte[] byteArray;

            byte[] zip;
            if (resData.get("COMPILED") instanceof BytesBlock){
                BytesBlock zipBlock = (BytesBlock) resData.get("COMPILED");
                byteArray = zipBlock.getBytes();
            }else{
                byteArray = (byte[]) resData.get("COMPILED");
            }

            InputStream compiledReport = new ByteArrayInputStream(byteArray);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(compiledReport);

            // Check special parameters (service, entity, pagesize)
            JRParameter[] params = jasperReport.getParameters();
            for (int i = 0; i < params.length; i++) {
                if (params[i].isForPrompting() && !params[i].isSystemDefined()) {
                    if (params[i].getName().equalsIgnoreCase("service")) {
                        index = params[i].getDefaultValueExpression().getText().length() - 1;
                        service = params[i].getDefaultValueExpression().getText().substring(1, index);
                    } else if (params[i].getName().equalsIgnoreCase("entity")) {
                        index = params[i].getDefaultValueExpression().getText().length() - 1;
                        entity = params[i].getDefaultValueExpression().getText().substring(1, index);
                    }
                    if (params[i].getName().equalsIgnoreCase("pagesize")) {
                        index = params[i].getDefaultValueExpression().getText().length() - 1;
                        pagesize = Integer.valueOf(params[i].getDefaultValueExpression().getText().substring(1, index));
                    }
                }
            }

            // Fill the report
            if (service == null) {
                // DB DataSource
                IReportDefinition rDef = this.parseReportEntityResult(this.getReportDefinition(reportId));
                is = this.reportFiller.fillReport(rDef, jasperReport, reportParameters, outputType, otherType, this.getBundle(),
                        this.getLocale(), dataSourceName);

            } else if (entity != null) {
                // Ontimize DataSource
                StringBuilder builder = new StringBuilder();
                List<Object> attributes = new ArrayList<>();
                for (JRField field : jasperReport.getFields()) {
                    attributes.add(field.getName());
                }
                Object bean = this.applicationContext.getBean(service);

                if (pagesize == null) {
                    // No pagination (EntityResultDataSource)
                    builder.append(entity).append("Query");
                    if (!reportParameters.isEmpty()) {
                        entityResult = (EntityResult) ReflectionTools.invoke(bean, builder.toString(), reportParameters, attributes);
                    } else {
                        entityResult = (EntityResult) ReflectionTools.invoke(bean, builder.toString(), keysValues, attributes);
                    }
                    EntityResultDataSource ods = new EntityResultDataSource(entityResult);
                    IReportDefinition rDef = this.parseReportEntityResult(this.getReportDefinition(reportId));
                    is = this.reportFiller.fillReport(rDef, jasperReport, reportParameters, outputType, otherType, this.getBundle(),
                            this.getLocale(), ods);

                } else {
                    // Pagination (AdvancedEntityResultDataSource)
                    builder.append(entity).append("PaginationQuery");
                    List<SQLOrder> order = new ArrayList<>();
                    JRGroup[] group = jasperReport.getGroups();
                    if (group != null) {
                        SQLOrder o;
                        for (int i = 0; i < group.length; i++) {
                            index = group[i].getExpression().getText().length() - 1;
                            o = new SQLOrder(group[i].getExpression().getText().substring(3, index));
                            order.add(o);
                        }
                    }
                    AdvancedEntityResultDataSource ods = new AdvancedEntityResultDataSource(bean, builder.toString(), reportParameters,
                            attributes, pagesize, 0, order);
                    IReportDefinition rDef = this.parseReportEntityResult(this.getReportDefinition(reportId));
                    is = this.reportFiller.fillReport(rDef, jasperReport, reportParameters, outputType, otherType, this.getBundle(),
                            this.getLocale(), ods);
                }
            }

            byte[] file = IOUtils.toByteArray(is);
            is.close();

            Map<String, Object> map = new HashMap<>();
            map.put("file", file);
            res.clear();
            res.addRecord(map);
            res.setCode(EntityResult.OPERATION_SUCCESSFUL);
            return CompletableFuture.completedFuture(res);

        } catch (ReportStoreException error) {
            throw error;
        } catch (Exception ex) {
            throw new ReportStoreException(ex);
        }

    }

    /*
     * (non-Javadoc)
     * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#fillReport(java.lang.Object, java.lang.String, java.util.Map,
     * com.ontimize.jee.server.services.reportstore.ReportOutputType, java.lang.String)
     */
    @Override
    public CompletableFuture<EntityResult> fillReport(Object reportId, String serviceName, Map<String, Object> reportParameters, String dataSourceName, ReportOutputType outputType,
                                                      String otherType)
            throws ReportStoreException {
        EntityResult res = new EntityResultMapImpl();
        try {
            IReportDefinition rDef = this.parseReportEntityResult(this.getReportDefinition(reportId));
            IReportAdapter adapter = this.applicationContext.getBean(IReportAdapter.class, serviceName);

            InputStream is = this.reportFiller.fillReport(rDef, this.getReportCompiledFolder(reportId), adapter, reportParameters, outputType, otherType,
                    this.getBundle(), this.getLocale(), dataSourceName);
            byte[] file = IOUtils.toByteArray(is);
            is.close();

            Map<String, Object> map = new HashMap<>();
            map.put("file", file);
            res.addRecord(map);
            res.setCode(EntityResult.OPERATION_SUCCESSFUL);
        } catch (IOException e) {
            res.setCode(1);
        }

        return CompletableFuture.completedFuture(res);

    }

    private void compileReport(Object reportId, IReportDefinition rDef) throws ReportStoreException {
        Path compileFolder = null;
        try {
            // Create temporary folder for the report
            Path reportFolder = this.getReportFolder(reportId);
            CheckingTools.failIf((reportFolder == null) || !Files.exists(reportFolder), DatabaseReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);
            this.safeCreateDirectories(reportFolder);

            Path reportFile = this.getReportFile(reportFolder, reportId);
            CheckingTools.failIf((reportFile == null), DatabaseReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);

            // Retrieve the report zip folder from the database (REPORT.ZIP)
            Map<String, Object> keyMap = new HashMap<>();
            keyMap.put(nameConvention.convertName("UUID"), rDef.getId());
            List<String> attrList = new ArrayList<>();
            attrList.add(nameConvention.convertName("ID"));
            attrList.add(nameConvention.convertName("ZIP"));
            EntityResult res = this.daoHelper.query(this.reportDao, keyMap, attrList);
            this.convertToUpperColumnsEntityResult(res);
            Map<?, ?> resData = res.getRecordValues(0);
            keyMap.clear();

            // Store the report zip folder in temporary folder

            byte[] zip;
            if (resData.get("ZIP") instanceof BytesBlock){
                BytesBlock zipBlock = (BytesBlock) resData.get("ZIP");
                zip = zipBlock.getBytes();
            }else{
                zip = (byte[]) resData.get("ZIP");
            }
            InputStream is = new ByteArrayInputStream(zip);
            try (OutputStream os = Files.newOutputStream(reportFile)) {
                IOUtils.copy(is, os);
            }
            CheckingTools.failIf(!Files.exists(reportFile), DatabaseReportStoreEngine.ERROR_GETTING_REPORT_SOURCE);

            // Create temporary folder for the compiled report
            compileFolder = this.getReportCompiledFolder(reportFolder, reportId);
            if (!Files.exists(compileFolder)) {
                this.safeCreateDirectories(compileFolder);
            } else if (!Files.isDirectory(compileFolder)) {
                Files.delete(compileFolder);
            } else {
                PathTools.deleteFolderContent(compileFolder);
            }

            // Compile the report
            rDef = this.reportCompiler.compile(reportFile, compileFolder, rDef);

            // Store the compiled report in the database (REPORT.COMPILED)
            Map<String, Object> attrMap = new HashMap<>();
            Integer id = (Integer) resData.get("ID");
            keyMap.put(this.nameConvention.convertName("ID"), id);
            byte[] compiledReport = Files.readAllBytes(compileFolder.resolve(rDef.getMainReportFileName() + DatabaseReportStoreEngine.JASPER));
            attrMap.put(this.nameConvention.convertName("COMPILED"), compiledReport);
            this.daoHelper.update(this.reportDao, attrMap, keyMap);
            attrMap.clear();

            // Parse the report parameters and store them in the database (REPORT_PARAMETER)
            List<ReportParameter> params = rDef.getParameters();
            for (int i = 0; i < params.size(); i++) {
                attrMap = this.parseReportParameterAttributes(params.get(i));
                attrMap.put(this.nameConvention.convertName("REPORT_ID"), id);
                this.daoHelper.insert(this.reportParameterDao, attrMap);
            }
        } catch (Exception ex) {
            try {
                PathTools.deleteFolderSafe(compileFolder);
            } catch (Exception ex2) {
                logger.warn("", ex2);
            }
            throw new ReportStoreException("E_COMPILING_REPORT", ex);
        }
    }

    /**
     * Gets the report folder.
     *
     * @param reportId the report id
     * @return the report folder
     */
    private Path getReportFolder(Object reportId) {
        try {
            String tDir = System.getProperty("java.io.tmpdir").concat(DatabaseReportStoreEngine.PREFIX + reportId.toString());
            Path path = Paths.get(tDir);
            if (!Files.exists(path))
                return Files.createDirectory(path);
            else return path;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the report file.
     *
     * @param reportFolder the report folder
     * @param reportId     the report id
     * @return the report file
     */
    private Path getReportFile(Path reportFolder, Object reportId) {
        return reportFolder.resolve(DatabaseReportStoreEngine.PREFIX + reportId.toString() + DatabaseReportStoreEngine.ZIP_EXTENSION);
    }

    /**
     * Gets the report compiled folder.
     *
     * @param reportId the report id
     * @return the report compiled folder
     */
    private Path getReportCompiledFolder(Object reportId) {
        return this.getReportFolder(reportId).resolve("compiled");
    }

    private Path getReportCompiledFolder(Path reportFolder, Object reportId) {
        return reportFolder.resolve("compiled");
    }

    /**
     * Gets the bundle.
     *
     * @return the bundle
     */
    public ResourceBundle getBundle() {
        return this.bundle;
    }

    /**
     * Sets the bundle.
     *
     * @param bundle the new bundle
     */
    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Sets the resource bundle.
     *
     * @param bundle the new resource bundle
     */
    public void setResourceBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Gets the locale.
     *
     * @return the locale
     */
    private Locale getLocale() {
//		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//		Locale locale = (Locale) request.getAttribute(OntimizeServletFilter.LOCALE);
//		if (locale == null) {
//			return Locale.getDefault();
//		}
//		return locale;
        return Locale.getDefault();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Load defaults if not configured
        if (this.reportCompiler == null) {
            this.reportCompiler = new JasperReportsCompilerFiller();
        }
        if (this.reportFiller == null) {
            if (this.reportCompiler instanceof IReportFiller) {
                this.reportFiller = (IReportFiller) this.reportCompiler;
            } else {
                this.reportFiller = new JasperReportsCompilerFiller();
            }
        }

        // Propague context (is required?!?)
        if (this.reportCompiler instanceof ApplicationContextAware) {
            ((ApplicationContextAware) this.reportCompiler).setApplicationContext(this.applicationContext);
        }
        if (this.reportFiller instanceof ApplicationContextAware) {
            ((ApplicationContextAware) this.reportFiller).setApplicationContext(this.applicationContext);
        }

        this.updateSettings();
    }

    /**
     * Parses the report attributes to a Map (no parameters).
     *
     * @param rDef the report definition object
     * @return the report attributes map (key, value)
     */
    private Map<String, Object> parseReportAttributes(IReportDefinition rDef) {
        Map<String, Object> attrMap = new HashMap<String, Object>();
        attrMap.put(this.nameConvention.convertName("UUID"), rDef.getId());
        attrMap.put(this.nameConvention.convertName("NAME"), rDef.getName());
        attrMap.put(this.nameConvention.convertName("DESCRIPTION"), rDef.getDescription());
        attrMap.put(this.nameConvention.convertName("REPORT_TYPE"), rDef.getType());
        attrMap.put(this.nameConvention.convertName("MAIN_REPORT_FILENAME"), rDef.getMainReportFileName());
        return attrMap;
    }

    /**
     * Parses the report attributes to a Map (with parameters).
     *
     * @param rDef the report definition object
     * @return the report attributes map (key, value)
     */
    private Map<String, Object> fillResponse(IReportDefinition rDef) {
        Map<String, Object> map = new HashMap<>();
        map.put(this.nameConvention.convertName("UUID"), rDef.getId());
        map.put(this.nameConvention.convertName("NAME"), rDef.getName());
        map.put(this.nameConvention.convertName("DESCRIPTION"), rDef.getDescription());
        map.put(this.nameConvention.convertName("MAIN_REPORT_FILENAME"), rDef.getMainReportFileName());
        map.put(this.nameConvention.convertName("REPORT_TYPE"), rDef.getType());
        map.put("PARAMETERS", rDef.getParameters());
        return map;
    }

    /**
     * Parses the report parameter attributes.
     *
     * @param param the report parameter definition object
     * @return the report parameter attributes map (key, value)
     */
    private Map<String, Object> parseReportParameterAttributes(ReportParameter param) {
        Map<String, Object> attrMap = new HashMap<>();
        attrMap.put(this.nameConvention.convertName("NAME"), param.getName());
        attrMap.put(this.nameConvention.convertName("DESCRIPTION"), param.getDescription());
        attrMap.put(this.nameConvention.convertName("NESTED_TYPE"), param.getType());
        attrMap.put(this.nameConvention.convertName("VALUE_CLASS"), param.getValueClass());
        return attrMap;
    }

    /**
     * Parses the result to a collection of report definition objects.
     *
     * @param res the report EntityResult
     * @return the report definition object collection
     */
    private IReportDefinition parseReportEntityResult(EntityResult res) {
        IReportDefinition rDef;
        String uuid, name, description, type, mainReportFilename;
        Map<?, ?> resData = res.getRecordValues(0);

        uuid = (String) resData.get(this.nameConvention.convertName("UUID"));
        name = (String) resData.get(this.nameConvention.convertName("NAME"));
        description = (String) resData.get(this.nameConvention.convertName("DESCRIPTION"));
        type = (String) resData.get(this.nameConvention.convertName("REPORT_TYPE"));
        mainReportFilename = (String) resData.get(this.nameConvention.convertName("MAIN_REPORT_FILENAME"));
        rDef = new BasicReportDefinition(uuid, name, description, type, mainReportFilename);
        return rDef;
    }

    /**
     * Parses the result to a list of report parameter definition objects.
     *
     * @param res the report parameter EntityResult
     * @return the report parameter definition object list
     */
    private List<ReportParameter> parseReportParameterEntityResult(EntityResult res) {
        final List<ReportParameter> paramList = new ArrayList<>();
        ReportParameter param;
        String name, description, nestedType, valueClass;

        int size = res.calculateRecordNumber();

        for (int i = 0; i < size; i++) {
            Map<?, ?> resData = res.getRecordValues(i);
            name = (String) resData.get("NAME");
            description = (String) resData.get("DESCRIPTION");
            valueClass = (String) resData.get("VALUE_CLASS");
            nestedType = (String) resData.get("NESTED_TYPE");

            param = new ReportParameter(name, description, valueClass, nestedType);
            paramList.add(param);
        }
        return paramList;
    }

    @Override
    public void updateSettings() {
        // TODO Auto-generated method stub

    }

}
